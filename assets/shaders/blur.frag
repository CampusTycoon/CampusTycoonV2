#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_blur_radius;

const float PIXEL_SIZE = 1.0 / 768.0;  // Moderate pixel size between previous versions

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);
    float radius = max(1.0, u_blur_radius);
    
    // 6x6 gaussian blur for moderate effect
    vec4 blurred = vec4(0.0);
    float total = 0.0;
    
    for(float x = -2.5; x <= 2.5; x += 1.0) {
        for(float y = -2.5; y <= 2.5; y += 1.0) {
            float weight = exp(-(x*x + y*y) / (2.0 * radius));
            vec2 offset = vec2(x, y) * PIXEL_SIZE * (radius * 0.8);  // Moderate radius multiplier
            blurred += texture2D(u_texture, v_texCoords + offset) * weight;
            total += weight;
        }
    }
    
    gl_FragColor = blurred / total;
} 