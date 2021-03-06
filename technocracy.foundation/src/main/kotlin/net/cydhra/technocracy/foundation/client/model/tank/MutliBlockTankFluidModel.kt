package net.cydhra.technocracy.foundation.client.model.tank

import com.google.common.collect.ImmutableList
import net.cydhra.technocracy.foundation.client.model.AbstractCustomModel
import net.cydhra.technocracy.foundation.client.model.pipe.PipeItemRedirector
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.common.model.IModelState
import java.util.function.Function


class MutliBlockTankFluidModel : AbstractCustomModel() {
    override fun bake(state: IModelState, format: VertexFormat, bakedTextureGetter: Function<ResourceLocation, TextureAtlasSprite>): IBakedModel {
        val bakedModel = ModelLoaderRegistry.getModel(MODEL_BASE!!).bake(state, format, bakedTextureGetter)
        return MutliBlockTankFluidModelBakery(bakedModel)
    }

    override fun getDependencies(): MutableCollection<ResourceLocation> {
        return ImmutableList.builder<ResourceLocation>().add(MODEL_BASE!!).build();
    }

    override fun getDefaultState(): IModelState {
        return ModelLoaderRegistry.getModel(MODEL_BASE!!).defaultState
    }
}