package io.lumine.mythic.api.adapters;
public interface AbstractItemStack {
    AbstractItemStack copy();
    AbstractItemStack amount(int a);
}