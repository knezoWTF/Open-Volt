#version 330 core

layout (location = 0) in vec2 a_position;

out vec2 fragCoord;

uniform vec2 u_resolution;

void main() {
    fragCoord = (a_position + 1.0) * 0.5 * u_resolution;
    gl_Position = vec4(a_position, 0.0, 1.0);
}