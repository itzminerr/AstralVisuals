#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    float depthBefore = texture(Sampler0, TexCoord).r;
    float depthAfter = texture(Sampler1, TexCoord).r;
    float mask = step(0.00001, depthBefore - depthAfter);
    OutColor = vec4(mask, mask, mask, mask);
}
