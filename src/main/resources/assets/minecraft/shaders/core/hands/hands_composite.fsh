#version 150

uniform sampler2D Sampler0;
uniform vec2 TexelSize;
uniform vec3 Color1;
uniform vec3 Color2;
uniform float Time;
uniform float WaveSpeed;
uniform float WaveScale;
uniform float Outline;
uniform float Glow;
uniform float Fill;
uniform float Alpha;
uniform float Mode;

in vec2 TexCoord;
in vec4 VertexColor;
out vec4 OutColor;

float maskAt(vec2 uv) {
    return smoothstep(0.05, 0.95, texture(Sampler0, uv).a);
}

float ringMaximum(vec2 uv, float radius) {
    vec2 axis = TexelSize * radius;
    vec2 diagonal = axis * 0.70710678;
    float value = 0.0;
    value = max(value, maskAt(uv + vec2(axis.x, 0.0)));
    value = max(value, maskAt(uv - vec2(axis.x, 0.0)));
    value = max(value, maskAt(uv + vec2(0.0, axis.y)));
    value = max(value, maskAt(uv - vec2(0.0, axis.y)));
    value = max(value, maskAt(uv + diagonal));
    value = max(value, maskAt(uv - diagonal));
    value = max(value, maskAt(uv + vec2(diagonal.x, -diagonal.y)));
    value = max(value, maskAt(uv + vec2(-diagonal.x, diagonal.y)));
    return value;
}

void main() {
    float center = maskAt(TexCoord);
    float outlineRadius = max(1.0, Outline);
    float outlineMask = max(0.0, ringMaximum(TexCoord, outlineRadius) - center) * step(0.001, Outline);

    float nearRadius = outlineRadius + 1.5 + Glow * 2.0;
    float farRadius = outlineRadius + 3.0 + Glow * 4.5;
    float nearGlow = max(0.0, ringMaximum(TexCoord, nearRadius) - center);
    float farGlow = max(0.0, ringMaximum(TexCoord, farRadius) - center);
    float glowMask = (nearGlow * 0.65 + farGlow * 0.35) * clamp(Glow * 0.45, 0.0, 1.0);

    float wave = 0.5 + 0.5 * sin((TexCoord.x + TexCoord.y * 0.72) * (10.0 * WaveScale) - Time * WaveSpeed * 2.5);
    vec3 prettyColor = mix(Color1, Color2, smoothstep(0.1, 0.9, wave));
    vec3 effectColor = mix(Color1, prettyColor, step(0.5, Mode));

    float inside = center * Fill;
    float edge = outlineMask * clamp(0.55 + Outline * 0.12, 0.0, 1.0);
    float resultAlpha = clamp(inside + edge + glowMask, 0.0, 1.0) * Alpha;
    if (resultAlpha <= 0.001) {
        discard;
    }

    OutColor = vec4(effectColor * VertexColor.rgb, resultAlpha * VertexColor.a);
}
