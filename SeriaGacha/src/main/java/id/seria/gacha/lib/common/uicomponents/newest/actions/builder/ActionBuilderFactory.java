package id.seria.gacha.lib.common.uicomponents.newest.actions.builder;

import id.seria.gacha.lib.common.utils.actions.ActionType;

@FunctionalInterface
public interface ActionBuilderFactory {
   ActionTypeBuilderLayout<?> create(ActionBuilderLayout var1, ActionType var2);
}
