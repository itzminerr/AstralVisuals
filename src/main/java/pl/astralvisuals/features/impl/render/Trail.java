package pl.astralvisuals.features.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10142;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import org.joml.Matrix4f;
import pl.astralvisuals.events.render.WorldRenderEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Trail {

    private final Cosmetic p;

    private final Map<UUID, State> map = new HashMap<>();

    public Trail(Cosmetic parent) {
        this.p = parent;
    }

    public void deactivate() {
        map.clear();
    }

    public void onWorldRender(WorldRenderEvent e) {
        if (p.mc.field_1687 == null || p.mc.field_1724 == null) return;
        if (!p.bodyTrailOn()) return;

        class_4587 ms = e.getStack();
        float pt = e.getPartialTicks();
        boolean camFirst = p.mc.field_1690.method_31044().method_31034();

        boolean allowSelf = Cosmetic.bool(p.bodyTrailSelf);
        boolean allowOthers = Cosmetic.bool(p.bodyTrailOthers);
        boolean allowFirst = Cosmetic.bool(p.bodyTrailFirstPerson);

        for (class_1657 pl : p.mc.field_1687.method_18456()) {
            if (pl == null) continue;
            if (pl.method_5767()) continue;

            boolean isSelf = pl == p.mc.field_1724;

            if (isSelf && !allowSelf) continue;
            if (!isSelf && !allowOthers) continue;
            if (isSelf && camFirst && !allowFirst) continue;

            renderBodyTrail(ms, pl, pt);
        }
    }

    private void renderBodyTrail(class_4587 ms, class_1657 player, float pt) {
        double px = class_3532.method_16436(pt, player.field_6014, player.method_23317());
        double py = class_3532.method_16436(pt, player.field_6036, player.method_23318());
        double pz = class_3532.method_16436(pt, player.field_5969, player.method_23321());

        float bodyYawDeg = class_3532.method_17821(pt, player.field_6220, player.field_6283);
        double bodyYawRad = Math.toRadians(bodyYawDeg);

        class_243 forward = new class_243(-Math.sin(bodyYawRad), 0.0, Math.cos(bodyYawRad));

        double baseY = py + player.method_17682() * 0.62;
        if (player.method_18276()) baseY -= player.method_17682() * 0.08;

        long now = System.currentTimeMillis();

        int limit = Math.max(6, Math.min(80, p.bodyTrailPoints.getInt()));
        long step = Math.max(6L, Math.min(80L, (long) p.bodyTrailStepMs.getInt()));

        double backOff = 0.34;
        class_243 rawPos = new class_243(px - forward.field_1352 * backOff, baseY, pz - forward.field_1350 * backOff);

        UUID uuid = player.method_5667();
        State st = map.computeIfAbsent(uuid, k -> new State());

        if (st.birthMs == 0L) st.birthMs = now;
        if (st.lastUpdateMs == 0L) st.lastUpdateMs = now;

        long dmsL = now - st.lastUpdateMs;
        if (dmsL < 0L) dmsL = 0L;
        if (dmsL > 80L) dmsL = 80L;
        st.lastUpdateMs = now;

        class_243 smoothPos = smooth(st.smoothPos, rawPos, (double) dmsL);
        st.smoothPos = smoothPos;

        if (st.lastEmitPos == null) {
            st.lastEmitPos = smoothPos;
            st.lastRawPos = smoothPos;
            st.accMs = 0.0;
            st.q.clear();
            st.q.addFirst(new Pt(smoothPos, now));
            st.q.addLast(new Pt(smoothPos, now));
        } else {
            st.accMs += (double) dmsL;

            class_243 from = st.lastRawPos == null ? st.lastEmitPos : st.lastRawPos;
            class_243 to = smoothPos;

            double span = Math.max(1.0, st.accMs);
            int loops = (int) Math.floor(span / (double) step);
            if (loops > 6) loops = 6;

            for (int i = 0; i < loops; i++) {
                double t = ((double) (i + 1) * (double) step) / span;
                if (t > 1.0) t = 1.0;
                class_243 pos = lerp(from, to, t);
                if (st.lastEmitPos == null || st.lastEmitPos.method_1025(pos) > 1.0E-8) {
                    st.lastEmitPos = pos;
                    st.q.addFirst(new Pt(pos, now));
                    while (st.q.size() > limit) st.q.removeLast();
                }
            }

            st.accMs = st.accMs - (double) loops * (double) step;
            if (st.accMs < 0.0) st.accMs = 0.0;
            st.lastRawPos = smoothPos;
        }

        if (st.q.size() < 3) return;

        float width = p.bodyTrailWidth.getValue();
        float alphaMul = p.bodyTrailAlpha.getValue();

        float fadeIn = (now - st.birthMs) / 240.0f;
        if (fadeIn < 0.0f) fadeIn = 0.0f;
        if (fadeIn > 1.0f) fadeIn = 1.0f;
        fadeIn = fadeIn * fadeIn;

        int cA = p.bodyTrailColorA.getColor();
        int cB = p.bodyTrailColorB.getColor();
        boolean grad = p.bodyTrailMode.isSelected("Градиент");

        RenderSystem.setShader(class_10142.field_53876);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(515);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.defaultBlendFunc();
        renderRibbon(ms, st, width, alphaMul * fadeIn, grad, cA, cB, false);

        RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
        renderRibbon(ms, st, width * 1.55f, alphaMul * fadeIn * 0.55f, grad, cA, cB, true);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderRibbon(class_4587 ms, State st,
                              float width, float alphaMul,
                              boolean grad, int cA, int cB,
                              boolean glowPass) {

        int n = st.q.size();
        ensureTmp(st, n);

        int i = 0;
        for (Pt p0 : st.q) st.tmp[i++] = p0.pos;

        int samples = countSamples(st.tmp, n);
        if (samples < 2) return;

        class_243 up = new class_243(0, 1, 0);

        Matrix4f m = ms.method_23760().method_23761();
        class_287 bb = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);

        int k = 0;
        for (int seg = 0; seg < n - 1; seg++) {
            class_243 p0 = st.tmp[Math.max(0, seg - 1)];
            class_243 p1 = st.tmp[seg];
            class_243 p2 = st.tmp[seg + 1];
            class_243 p3 = st.tmp[Math.min(n - 1, seg + 2)];

            double d = p1.method_1022(p2);
            int steps = class_3532.method_15340((int) Math.ceil(d / 0.06), 3, 9);

            if (seg == 0) {
                emit(bb, m, p1, up, width, alphaMul, grad, cA, cB, k, samples, glowPass);
                k++;
            }

            for (int s = 1; s <= steps; s++) {
                double t = s / (double) steps;
                class_243 p = catmullRom(p0, p1, p2, p3, t);
                emit(bb, m, p, up, width, alphaMul, grad, cA, cB, k, samples, glowPass);
                k++;
            }
        }

        class_286.method_43433(bb.method_60800());
    }

    private void emit(class_287 bb, Matrix4f m, class_243 p0, class_243 up,
                      float width, float alphaMul,
                      boolean grad, int cA, int cB,
                      int k, int samples, boolean glowPass) {

        float tt = samples <= 1 ? 0.0f : (k / (float) (samples - 1));
        float fade = 1.0f - tt;
        fade = fade * fade;

        int col = grad ? lerpArgb(cA, cB, tt) : cA;

        float rr = ((col >>> 16) & 0xFF) / 255.0f;
        float gg = ((col >>> 8) & 0xFF) / 255.0f;
        float bb0 = (col & 0xFF) / 255.0f;

        float a = ((col >>> 24) & 0xFF) / 255.0f;
        float aMul = glowPass ? 0.85f : 0.90f;
        a = class_3532.method_15363(a * (aMul * fade) * alphaMul, 0.0f, 1.0f);

        float hw = (width * 0.5f) * (0.55f + 0.75f * fade);
        class_243 off = up.method_1021(hw);

        class_243 l = p0.method_1019(off);
        class_243 r = p0.method_1020(off);

        bb.method_22918(m, (float) l.field_1352, (float) l.field_1351, (float) l.field_1350).method_22915(rr, gg, bb0, a);
        bb.method_22918(m, (float) r.field_1352, (float) r.field_1351, (float) r.field_1350).method_22915(rr, gg, bb0, a);
    }

    private int countSamples(class_243[] in, int n) {
        if (n < 2) return 0;
        if (n == 2) return 3;

        int total = 0;
        for (int i = 0; i < n - 1; i++) {
            class_243 p1 = in[i];
            class_243 p2 = in[i + 1];
            double d = p1.method_1022(p2);
            int steps = class_3532.method_15340((int) Math.ceil(d / 0.06), 3, 9);
            if (i == 0) total += 1;
            total += steps;
        }
        return total;
    }

    private void ensureTmp(State st, int n) {
        if (st.tmp == null || st.tmp.length < n) st.tmp = new class_243[n];
    }

    private class_243 catmullRom(class_243 p0, class_243 p1, class_243 p2, class_243 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double x =
                0.5 * ((2.0 * p1.field_1352) +
                        (-p0.field_1352 + p2.field_1352) * t +
                        (2.0 * p0.field_1352 - 5.0 * p1.field_1352 + 4.0 * p2.field_1352 - p3.field_1352) * t2 +
                        (-p0.field_1352 + 3.0 * p1.field_1352 - 3.0 * p2.field_1352 + p3.field_1352) * t3);

        double y =
                0.5 * ((2.0 * p1.field_1351) +
                        (-p0.field_1351 + p2.field_1351) * t +
                        (2.0 * p0.field_1351 - 5.0 * p1.field_1351 + 4.0 * p2.field_1351 - p3.field_1351) * t2 +
                        (-p0.field_1351 + 3.0 * p1.field_1351 - 3.0 * p2.field_1351 + p3.field_1351) * t3);

        double z =
                0.5 * ((2.0 * p1.field_1350) +
                        (-p0.field_1350 + p2.field_1350) * t +
                        (2.0 * p0.field_1350 - 5.0 * p1.field_1350 + 4.0 * p2.field_1350 - p3.field_1350) * t2 +
                        (-p0.field_1350 + 3.0 * p1.field_1350 - 3.0 * p2.field_1350 + p3.field_1350) * t3);

        return new class_243(x, y, z);
    }

    private class_243 smooth(class_243 prev, class_243 raw, double dtMs) {
        if (prev == null) return raw;

        double tau = 115.0;
        double a = 1.0 - Math.exp(-dtMs / tau);
        if (a < 0.0) a = 0.0;
        if (a > 1.0) a = 1.0;

        double x = prev.field_1352 + (raw.field_1352 - prev.field_1352) * a;
        double y = prev.field_1351 + (raw.field_1351 - prev.field_1351) * a;
        double z = prev.field_1350 + (raw.field_1350 - prev.field_1350) * a;

        return new class_243(x, y, z);
    }

    private class_243 lerp(class_243 a, class_243 b, double t) {
        if (t <= 0.0) return a;
        if (t >= 1.0) return b;
        return new class_243(
                a.field_1352 + (b.field_1352 - a.field_1352) * t,
                a.field_1351 + (b.field_1351 - a.field_1351) * t,
                a.field_1350 + (b.field_1350 - a.field_1350) * t
        );
    }

    private int lerpArgb(int a, int b, float t) {
        float p0 = class_3532.method_15363(t, 0.0f, 1.0f);

        int aa = (a >>> 24) & 0xFF;
        int ar = (a >>> 16) & 0xFF;
        int ag = (a >>> 8) & 0xFF;
        int ab = a & 0xFF;

        int ba = (b >>> 24) & 0xFF;
        int br = (b >>> 16) & 0xFF;
        int bg = (b >>> 8) & 0xFF;
        int bb = b & 0xFF;

        int ra = (int) (aa + (ba - aa) * p0);
        int rr = (int) (ar + (br - ar) * p0);
        int rg = (int) (ag + (bg - ag) * p0);
        int rb = (int) (ab + (bb - ab) * p0);

        ra = class_3532.method_15340(ra, 0, 255);
        rr = class_3532.method_15340(rr, 0, 255);
        rg = class_3532.method_15340(rg, 0, 255);
        rb = class_3532.method_15340(rb, 0, 255);

        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    private static final class State {
        final ArrayDeque<Pt> q = new ArrayDeque<>();
        long birthMs = 0L;
        long lastUpdateMs = 0L;
        double accMs = 0.0;
        class_243 smoothPos = null;
        class_243 lastEmitPos = null;
        class_243 lastRawPos = null;
        class_243[] tmp = null;
    }

    private static final class Pt {
        final class_243 pos;
        final long time;

        Pt(class_243 pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }
}
