package pl.astralvisuals.features.impl.render;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_742;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.features.module.setting.implement.TextSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;

/*
 * Cosmetic — космет-модуль (порт из Cosmetic V2.0). На данном этапе портированы
 * геометрические косметики, рисуемые через WorldRenderEvent: Сферы, Нимб и Линия тела.
 * Питомцы (Raven) и анимации игрока (теневые клоны / Naruto-бег) рендерят живые
 * модели сущностей и портируются отдельным проходом.
 */
public class Cosmetic extends Module {

    final class_2960 defaultTexture = class_2960.method_60654("textures/features/particles/bloom.png");

    final BooleanSetting spheresEnabled = new BooleanSetting("Сферы", "Включить сферы").setValue(false);
    final BooleanSetting haloEnabled = new BooleanSetting("Нимб", "Включить Нимб").setValue(false);
    final BooleanSetting bodyTrailEnabled = new BooleanSetting("Линия", "Включить Линию тела").setValue(false);
    final BooleanSetting capeEnabled = new BooleanSetting("Кастомный плащ", "Использовать PNG из папки Custom/Capes").setValue(false);
    final BooleanSetting capeOthers = new BooleanSetting("Плащ на других", "Показывать выбранный плащ и на других игроках")
            .setValue(false).visible(this::capeOn);
    final TextSetting capeFile = new TextSetting("Файл плаща", "Имя PNG в AstralVisuals/Custom/Capes (лучше 64x32 или 64x64)")
            .setText("cape.png").setMin(1).setMax(96).visible(this::capeOn);
    private String loadedCapeKey = "";
    private class_2960 loadedCapeTexture;

    final BooleanSetting haloOthers = new BooleanSetting("Нимб на других", "Рисовать Нимб на других игроках").setValue(false).visible(this::haloOn);
    final BooleanSetting haloFirstPerson = new BooleanSetting("Нимб от 1-го лица", "Рисовать Нимб от первого лица").setValue(true).visible(this::haloOn);

    final SliderSettings haloDistance = new SliderSettings("Дистанция Нимба", "Макс. дистанция отрисовки Нимба")
            .setValue(72).range(8, 192)
            .visible(this::haloOn);

    final SelectSetting haloQuality = new SelectSetting("Качество Нимба", "Детализация Нимба")
            .value("Низкое", "Среднее", "Высокое", "Ультра")
            .selected("Среднее")
            .visible(this::haloOn);

    final ColorSetting haloColor = new ColorSetting("Цвет Нимба", "Цвет Нимба")
            .value(ColorAssist.getColor(255, 255, 255, 255))
            .visible(this::haloOn);

    final BooleanSetting haloAnimate = new BooleanSetting("Анимация Нимба", "Плавное покачивание Нимба")
            .setValue(true)
            .visible(this::haloOn);

    final SliderSettings haloRadius = new SliderSettings("Радиус Нимба", "Большой радиус Нимба")
            .setValue(0.36f).range(0.18f, 0.90f)
            .visible(this::haloOn);

    final SliderSettings haloThickness = new SliderSettings("Толщина Нимба", "Толщина трубки Нимба")
            .setValue(0.06f).range(0.02f, 0.16f)
            .visible(this::haloOn);

    final SliderSettings haloYOffset = new SliderSettings("Смещение Нимба по Y", "Смещение Нимба по высоте")
            .setValue(0.10f).range(-0.60f, 0.60f)
            .visible(this::haloOn);

    final SelectSetting mode = new SelectSetting("Режим", "Режим движения сфер")
            .value("Статичный", "Орбита", "Спираль", "Спираль V2", "Волны")
            .selected("Статичный")
            .visible(this::spheresOn);

    final SelectSetting effect = new SelectSetting("Эффект", "Доп. эффект")
            .value("Нет", "След", "Огненный след")
            .selected("След")
            .visible(this::spheresOn);

    final ColorSetting exort = new ColorSetting("Экзорт", "Цвет сферы")
            .value(ColorAssist.getColor(255, 200, 100, 200))
            .visible(this::spheresOn);

    final ColorSetting wex = new ColorSetting("Векс", "Цвет сферы")
            .value(ColorAssist.getColor(255, 150, 255, 200))
            .visible(this::spheresOn);

    final ColorSetting quas = new ColorSetting("Квас", "Цвет сферы")
            .value(ColorAssist.getColor(150, 150, 255, 200))
            .visible(this::spheresOn);

    final SliderSettings size = new SliderSettings("Размер", "Размер сферы")
            .setValue(0.12f).range(0.05f, 0.22f)
            .visible(this::spheresOn);

    final SliderSettings radius = new SliderSettings("Радиус", "Радиус орбиты сфер")
            .setValue(0.42f).range(0.18f, 1.25f)
            .visible(this::spheresOn);

    final SliderSettings height = new SliderSettings("Высота", "Смещение по Y")
            .setValue(0.05f).range(-1.20f, 3.50f)
            .visible(this::spheresOn);

    final SliderSettings speed = new SliderSettings("Скорость", "Скорость анимации")
            .setValue(1.85f).range(0.2f, 4.0f)
            .visible(() -> spheresOn() && !mode.isSelected("Статичный"));

    final SliderSettings breathe = new SliderSettings("Пульсация", "Пульсация радиуса")
            .setValue(0.08f).range(0.0f, 0.30f)
            .visible(this::spheresOn);

    final SliderSettings spiralHeight = new SliderSettings("Высота спирали", "Размах спирали по Y")
            .setValue(1.25f).range(0.2f, 6.0f)
            .visible(() -> spheresOn() && (mode.isSelected("Спираль") || mode.isSelected("Спираль V2")));

    final SliderSettings waveAmp = new SliderSettings("Амплитуда волн", "Амплитуда волн по Y")
            .setValue(0.22f).range(0.0f, 1.2f)
            .visible(() -> spheresOn() && mode.isSelected("Волны"));

    final SliderSettings waveFreq = new SliderSettings("Частота волн", "Частота волн")
            .setValue(2.4f).range(0.6f, 8.0f)
            .visible(() -> spheresOn() && mode.isSelected("Волны"));

    final SelectSetting sphereEditor = new SelectSetting("Редактор", "Какую сферу редактировать")
            .value("Экзорт", "Векс", "Квас", "Все")
            .selected("Все")
            .visible(this::spheresOn);

    final SliderSettings editLR = new SliderSettings("Смещение Лево/Право", "Смещение по правой оси")
            .setValue(0.0f).range(-1.50f, 1.50f)
            .visible(this::spheresOn);

    final SliderSettings editFB = new SliderSettings("Смещение Вперёд/Назад", "Смещение по оси вперёд/назад")
            .setValue(0.0f).range(-1.50f, 1.50f)
            .visible(this::spheresOn);

    final SliderSettings trailLen = new SliderSettings("Длина следа", "Количество точек")
            .setValue(18).range(6, 48)
            .visible(() -> spheresOn() && !effect.isSelected("Нет"));

    final SliderSettings trailPower = new SliderSettings("Сила свечения", "Интенсивность свечения")
            .setValue(1.10f).range(0.2f, 2.2f)
            .visible(() -> spheresOn() && !effect.isSelected("Нет"));

    final BooleanSetting self = new BooleanSetting("На себя", "Рисовать на себе").setValue(true).visible(this::spheresOn);
    final BooleanSetting others = new BooleanSetting("На других", "Рисовать на других").setValue(false).visible(this::spheresOn);
    final BooleanSetting firstPerson = new BooleanSetting("От 1-го лица", "Рисовать от 1-го лица").setValue(true).visible(this::spheresOn);

    final SelectSetting bodyTrailMode = new SelectSetting("Режим Линии", "Стиль Линии")
            .value("Статичный", "Градиент")
            .selected("Статичный")
            .visible(this::bodyTrailOn);

    final ColorSetting bodyTrailColorA = new ColorSetting("Цвет Линии", "Цвет Линии")
            .value(ColorAssist.getColor(255, 255, 255, 200))
            .visible(this::bodyTrailOn);

    final ColorSetting bodyTrailColorB = new ColorSetting("Цвет Линии 2", "Второй цвет градиента")
            .value(ColorAssist.getColor(255, 140, 255, 200))
            .visible(() -> bodyTrailOn() && bodyTrailMode.isSelected("Градиент"));

    final SliderSettings bodyTrailPoints = new SliderSettings("Точки Линии", "Количество точек")
            .setValue(24).range(6, 80)
            .visible(this::bodyTrailOn);

    final SliderSettings bodyTrailWidth = new SliderSettings("Ширина Линии", "Толщина Линии")
            .setValue(0.22f).range(0.06f, 0.60f)
            .visible(this::bodyTrailOn);

    final SliderSettings bodyTrailAlpha = new SliderSettings("Прозрачность Линии", "Множитель альфы")
            .setValue(1.0f).range(0.1f, 1.6f)
            .visible(this::bodyTrailOn);

    final SliderSettings bodyTrailStepMs = new SliderSettings("Шаг (мс)", "Интервал добавления точки (мс)")
            .setValue(18).range(6, 80)
            .visible(this::bodyTrailOn);

    final BooleanSetting bodyTrailSelf = new BooleanSetting("Линия на себя", "Рисовать на себе").setValue(true).visible(this::bodyTrailOn);
    final BooleanSetting bodyTrailOthers = new BooleanSetting("Линия на других", "Рисовать на других").setValue(false).visible(this::bodyTrailOn);
    final BooleanSetting bodyTrailFirstPerson = new BooleanSetting("Линия от 1-го лица", "Рисовать от 1-го лица").setValue(true).visible(this::bodyTrailOn);

    final Nimb nimb;
    final Spheres spheres;
    final Trail trail;

    public static Cosmetic getInstance() {
        return Instance.get(Cosmetic.class);
    }

    public Cosmetic() {
        super("Cosmetic", "Cosmetic", ModuleCategory.RENDER);

        setup(
                spheresEnabled, haloEnabled, bodyTrailEnabled, capeEnabled, capeOthers, capeFile,

                mode, effect,
                exort, wex, quas,
                size, radius, height,
                speed, breathe,
                spiralHeight, waveAmp, waveFreq,
                sphereEditor, editLR, editFB,
                trailLen, trailPower,
                self, others, firstPerson,

                haloOthers, haloFirstPerson,
                haloDistance, haloQuality,
                haloColor, haloAnimate,
                haloRadius, haloThickness, haloYOffset,

                bodyTrailMode,
                bodyTrailColorA, bodyTrailColorB,
                bodyTrailPoints, bodyTrailWidth, bodyTrailAlpha, bodyTrailStepMs,
                bodyTrailSelf, bodyTrailOthers, bodyTrailFirstPerson
        );

        this.nimb = new Nimb(this);
        this.spheres = new Spheres(this);
        this.trail = new Trail(this);
    }

    @Override
    public void activate() {
        capeDirectory().mkdirs();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (spheres != null) spheres.deactivate();
        if (nimb != null) nimb.deactivate();
        if (trail != null) trail.deactivate();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.field_1687 == null || mc.field_1724 == null) return;

        boolean s = spheresOn();
        boolean h = haloOn();
        boolean t = bodyTrailOn();

        if (!s && !h && !t) return;

        if (s) spheres.onWorldRender(e);
        if (h) nimb.onWorldRender(e);
        if (t) trail.onWorldRender(e);
    }

    boolean spheresOn() {
        return bool(spheresEnabled);
    }

    boolean haloOn() {
        return bool(haloEnabled);
    }

    boolean bodyTrailOn() {
        return bool(bodyTrailEnabled);
    }

    boolean capeOn() {
        return bool(capeEnabled);
    }

    /** Возвращает динамически зарегистрированную текстуру плаща для render state игрока. */
    public synchronized class_2960 getCapeTexture(class_742 player) {
        if (!isState() || !capeOn() || player == null || mc.field_1724 == null
                || player != mc.field_1724 && !capeOthers.isValue()) {
            return null;
        }

        File directory = capeDirectory();
        directory.mkdirs();
        String configured = capeFile.getText();
        String safeName = new File(configured == null || configured.isBlank() ? "cape.png" : configured).getName();
        File file = new File(directory, safeName);
        String key = file.getAbsolutePath() + ':' + (file.isFile() ? file.lastModified() : -1L);
        if (key.equals(this.loadedCapeKey)) {
            return this.loadedCapeTexture;
        }

        this.loadedCapeKey = key;
        this.loadedCapeTexture = null;
        if (!file.isFile() || !safeName.toLowerCase().endsWith(".png")) {
            return null;
        }

        try (FileInputStream stream = new FileInputStream(file)) {
            class_1011 image = class_1011.method_4309(stream);
            class_1043 texture = new class_1043(image);
            class_2960 id = class_2960.method_60655("astralvisuals", "custom_capes/" + Integer.toHexString(key.hashCode()));
            mc.method_1531().method_4616(id, texture);
            this.loadedCapeTexture = id;
        } catch (Exception ignored) {
            this.loadedCapeTexture = null;
        }

        return this.loadedCapeTexture;
    }

    public static File capeDirectory() {
        return new File(new File(mc.field_1697, "AstralVisuals/Custom"), "Capes");
    }

    public static boolean bool(Object setting) {
        if (setting == null) return false;

        try {
            Method m = setting.getClass().getMethod("getValue");
            Object v = m.invoke(setting);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {
        }

        try {
            Method m = setting.getClass().getMethod("isValue");
            Object v = m.invoke(setting);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {
        }

        try {
            Method m = setting.getClass().getMethod("isEnabled");
            Object v = m.invoke(setting);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {
        }

        try {
            Method m = setting.getClass().getMethod("isState");
            Object v = m.invoke(setting);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {
        }

        try {
            Method m = setting.getClass().getMethod("get");
            Object v = m.invoke(setting);
            if (v instanceof Boolean) return (Boolean) v;
        } catch (Exception ignored) {
        }

        for (String fn : new String[]{"value", "state", "enabled"}) {
            try {
                Field f = setting.getClass().getDeclaredField(fn);
                f.setAccessible(true);
                Object v = f.get(setting);
                if (v instanceof Boolean) return (Boolean) v;
            } catch (Exception ignored) {
            }
        }

        return false;
    }
}
