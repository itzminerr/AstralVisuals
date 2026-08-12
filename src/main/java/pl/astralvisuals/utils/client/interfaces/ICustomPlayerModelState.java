package pl.astralvisuals.utils.client.interfaces;

// реализуется миксином на PlayerEntityRenderState (class_10055) — переносит флаг
// "рисовать кастомную модель" из состояния рендера игрока в рендерер модели/брони.
public interface ICustomPlayerModelState {
   boolean astral$hasCustomModel();

   String astral$getCustomModel();

   void astral$setCustomModel(boolean enabled, String model);
}
