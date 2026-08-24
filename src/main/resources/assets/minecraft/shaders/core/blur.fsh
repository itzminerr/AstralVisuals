#version 150

#define TAU 6.28318530718

uniform vec2 size;
uniform vec2 location;
uniform vec4 radius;
uniform float thickness;
uniform float softness;

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform float Quality;

uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;
uniform vec4 outlineColor;

in vec2 texCoord;
out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, vec4 radius) {
    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
    radius.x  = (center.y > 0.0) ? radius.x : radius.y;

    vec2 q = abs(center) - size + radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4){
    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
    color += mix(0.0019607843, -0.0019607843, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
    return color;
}

// scale управляет силой размытия: 1.0 — обычное, больше — сильнее.
vec3 sampleBlur(vec2 uv, float scale) {
    vec2 Radius = Quality / InputResolution.xy * scale;
    vec4 acc = texture(InputSampler, uv);
    float step = TAU / 16.0;

    for (float d = 0.0; d < TAU; d += step) {
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            acc += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    acc /= 80.0;
    return acc.rgb;
}

vec4 blur() {
    vec4 rectColor = createGradient((gl_FragCoord.xy - location) / size, color1, color2, color3, color4);

    vec2 hs = size / 2.0;
    vec2 centerLocal = gl_FragCoord.xy - location - hs;
    float dist = roundedBoxSDF(centerLocal, hs, radius);
    float halfMin = max(min(hs.x, hs.y), 1.0);

    // 0 в центре, 1 у самого края.
    float toEdge = clamp(1.0 + dist / halfMin, 0.0, 1.0);

    // Нормаль SDF (наружу) — направление преломления.
    float e = 1.0;
    float gx = roundedBoxSDF(centerLocal + vec2(e, 0.0), hs, radius) - roundedBoxSDF(centerLocal - vec2(e, 0.0), hs, radius);
    float gy = roundedBoxSDF(centerLocal + vec2(0.0, e), hs, radius) - roundedBoxSDF(centerLocal - vec2(0.0, e), hs, radius);
    vec2 n = normalize(vec2(gx, gy) + 1e-5);

    vec2 baseUv = gl_FragCoord.xy / InputResolution.xy;
    vec2 dir = n / InputResolution.xy;

    // Размытие: обычное в центре, сильное у краёв (как в примерах).
    float blurScale = 1.0 + smoothstep(0.2, 1.0, toEdge) * 2.6;

    // Преломление-«линза» концентрируется у самой кромки.
    float refr = pow(toEdge, 3.0);
    float disp = refr * (halfMin * 0.22 + 8.0);

    vec3 col = sampleBlur(baseUv + dir * disp, blurScale);

    // Хроматическая дисперсия у края — заметная, но без перебора с радугой.
    float chroma = refr * (halfMin * 0.10 + 3.0);
    float chromaMix = refr * 0.8;
    col.r = mix(col.r, sampleBlur(baseUv + dir * (disp + chroma), blurScale).r, chromaMix);
    col.b = mix(col.b, sampleBlur(baseUv + dir * (disp - chroma), blurScale).b, chromaMix);

    // Лёгкий блик-Френель у кромки.
    col += refr * refr * 0.05;

    // Приглушаем размытый фон под стеклом (как в Kimiko): сохраняет контраст текста.
    col *= 0.78;

    // Стекло непрозрачно по альфе: резкий фон полностью заменяется размытой версией,
    // frost-цвет лишь слегка подкрашивает. «Прозрачность» = виден размытый мир, а не дыра.
    vec3 tinted = mix(col, rectColor.rgb, rectColor.a);
    return vec4(tinted, 1.0);
}

void main() {
    float distance = roundedBoxSDF(gl_FragCoord.xy - location - (size / 2.0), size / 2.0, radius);
    float smoothedAlpha = 1.0 - smoothstep(-1.0, thickness > 0. ? 1. : softness + 1., distance);

    if(smoothedAlpha < 0.49 && thickness > 0.) {
        float smoothedborderAlpha = (1.0 - smoothstep(-softness,  softness, distance));
        fragColor = vec4(outlineColor.rgb, smoothedborderAlpha * outlineColor.a);
    } else {
        float borderAlpha = 1.0 - smoothstep(thickness - 2.0, thickness, abs(distance));
        vec4 blur = blur();
        vec4 basicColor = vec4(blur.rgb, blur.a * smoothedAlpha);
        fragColor = mix(vec4(blur.rgb, 0.), mix(basicColor, thickness > 0. ? outlineColor : basicColor, borderAlpha), smoothedAlpha);
    }
}
