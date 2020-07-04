package de.maxhenkel.reap;

import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collections;
import java.util.List;

public class SingleTag<T extends ForgeRegistryEntry> implements ITag.INamedTag<T> {

    private T element;

    public SingleTag(T element) {
        this.element = element;
    }

    @Override
    public ResourceLocation func_230234_a_() {
        return element.getRegistryName();
    }

    @Override
    public boolean func_230235_a_(T element) {
        return this.element == element;
    }

    @Override
    public List<T> func_230236_b_() {
        return Collections.singletonList(element);
    }
}
