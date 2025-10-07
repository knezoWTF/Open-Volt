#version 330 core

in vec2 fragCoord;
out vec4 fragColor;

uniform vec2 u_resolution;
uniform vec2 u_position;
uniform vec2 u_size;
uniform float u_radius;
uniform vec4 u_color;

float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
}

void main() {
    vec2 uv = fragCoord;
    vec2 halfSize = u_size * 0.5;
    vec2 center = u_position + halfSize;
    
    vec2 pos = uv - center;
    
    float distance = roundedBoxSDF(pos, halfSize, u_radius);
    
    float alpha = 1.0 - smoothstep(-1.0, 1.0, distance);
    
    fragColor = vec4(u_color.rgb, u_color.a * alpha);
}