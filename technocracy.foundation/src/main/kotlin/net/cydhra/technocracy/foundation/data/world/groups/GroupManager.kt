package net.cydhra.technocracy.foundation.data.world.groups

import net.cydhra.technocracy.foundation.data.world.api.AbstractSaveDataElement
import net.cydhra.technocracy.foundation.data.world.api.DataManager
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.Constants
import java.util.*

object GroupManager : AbstractSaveDataElement("ownership") {
    val groups = mutableListOf<PlayerGroup>()
    val playerPriories = mutableMapOf<UUID, MutableList<UUID>>()

    @Suppress("NAME_SHADOWING")
    override fun deserializeNBT(nbt: NBTTagCompound) {
        groups.clear()
        playerPriories.clear()

        val groups = nbt.getCompoundTag("groups")
        val priorities = nbt.getTagList("userPriority", Constants.NBT.TAG_COMPOUND)

        for (key in groups.keySet) {
            val ownership = PlayerGroup(UUID.fromString(key))
            val list = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND)
            for (i in 0 until list.tagCount()) {
                val comp = list.getCompoundTagAt(i)
                ownership.users[comp.getUniqueId("uuid")!!] = PlayerGroup.GroupRights.valueOf(comp.getString("rights"))
            }
            GroupManager.groups.add(ownership)
        }

        for (id in 0 until priorities.tagCount()) {
            val playerPriority = priorities.getCompoundTagAt(id)
            val playerUUID = playerPriority.getUniqueId("uuid")!!
            val order = playerPriority.getTagList("order", Constants.NBT.TAG_COMPOUND)
            val orderList = mutableListOf<UUID>()
            for (id in 0 until order.tagCount()) {
                val playerPriority = priorities.getCompoundTagAt(id)
                orderList.add(playerPriority.getUniqueId("uuid")!!)
            }
            orderList.reverse()
            playerPriories[playerUUID] = orderList
        }
    }

    override fun serializeNBT(): NBTTagCompound {
        val list = NBTTagCompound()
        val groups = NBTTagCompound()
        val priorities = NBTTagList()

        for (os in GroupManager.groups) {
            val ownership = NBTTagList()

            for (user in os.users) {
                val userTag = NBTTagCompound()

                userTag.setUniqueId("uuid", user.key)
                userTag.setString("rights", user.value.name)

                ownership.appendTag(userTag)
            }
            groups.setTag(os.groupId.toString(), ownership)
        }

        for (pair in playerPriories) {
            val playerPrioTag = NBTTagCompound()
            val order = NBTTagList()

            for (groupOrder in pair.value) {
                val inner = NBTTagCompound()
                inner.setUniqueId("uuid", groupOrder)
                order.appendTag(inner)
            }

            playerPrioTag.setUniqueId("uuid", pair.key)
            playerPrioTag.setTag("order", order)
            priorities.appendTag(playerPrioTag)
        }

        list.setTag("groups", groups)
        list.setTag("userPriority", priorities)
        return list
    }

    /**
     * @param user the uuid of the player
     * @return the group the player has prioritized, if none is available it generates one
     */
    fun getGroupFromUser(user: UUID): PlayerGroup {
        val playersGroups = playerPriories.getOrPut(user) { mutableListOf() }

        val group = getGroup(playersGroups.getOrElse(0) {
            //no best group found, generate one
            genGroup(user).groupId
        })

        if (group == null) {
            //group does not exist, remove it and retry
            playersGroups.removeAt(0)
            return getGroupFromUser(user)
        }
        return group
    }

    /**
     * Generates a new group for a user
     *
     * @param owner the uuid of the group owner
     * @return the new group that was generated
     */
    private fun genGroup(owner: UUID): PlayerGroup {
        val id = UUID.randomUUID()
        val group = PlayerGroup(id)
        //give owner the owner rights
        group.users[owner] = PlayerGroup.GroupRights.OWNER
        //add group to group list
        groups.add(group)
        //set the new group to the nr 1 of the user
        playerPriories[owner]!!.add(id)
        //save changes
        DataManager.manager!!.markDirty()
        return group
    }

    /**
     * @param groupID the uuid of the group
     * @return the group with the uuid, null if none was found
     */
    fun getGroup(groupID: UUID): PlayerGroup? {
        return groups.find { it.groupId == groupID }
    }

    /**
     * Updates the rights a user has or adds the user, if the executor has enough rights
     *
     * @param groupID the uuid of the group
     * @param executor the player changing the rights
     * @param updateUser the user that gets updated
     * @param newRight the new rights the user gets
     *
     * @return true if the update was successful
     */
    fun updateUserAccess(groupID: UUID, executor: UUID, updateUser: UUID, newRight: PlayerGroup.GroupRights): Boolean {
        val group = groups.find { it.groupId == groupID }
        if (group != null) {
            val executorRank = group.getRights(executor)
            val currentRank = group.getRights(updateUser)
            //only allow edit if executor has rights and is above the other user
            if (executorRank.power > 0 && executorRank.power > currentRank.power) {
                group.users[updateUser] = newRight
                DataManager.manager!!.markDirty()
                return true
            }
        }
        return false
    }

    class PlayerGroup(val groupId: UUID) {
        val users = mutableMapOf<UUID, GroupRights>()

        fun getRights(user: UUID): GroupRights {
            return users[user] ?: GroupRights.NONE
        }

        enum class GroupRights(val power: Int) {
            OWNER(10), MANAGE(5), USER(0), NONE(-1)
        }
    }

    override fun reset() {
        groups.clear()
        playerPriories.clear()
    }
}