#version 300 es
#define EPSIRON 0.03

precision mediump float;

uniform float uTime;

in vec2 textureCoord;
out vec4 fragColor;

const float postNum = 8.;
const float PI = 3.14159;

float kantan(float x, float y, float ix, float iy, float slope) {
    if (abs(y - iy - slope * (x - ix)) < EPSIRON) {
        //if (abs(x - ix) < EPSIRON) {
        return 1.0;
    } else {
        return 0.0;
    }
}

float judge(float x, float y, float layerIndex) {
    if (abs(layerIndex-0.0) < 0.01) {
        if (abs(x-0.5) < 2. * EPSIRON) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
    float slope = - (1. / postNum) * pow(2., layerIndex + 1.);
    float iy = (layerIndex+1.) / postNum;
    float ix = 1.0 / pow(2., layerIndex+1.);

    float ret = 0.0;
    float n = pow(2., layerIndex + 1.);
    for (int i=0; i<int(n); i++) {
        ix = (2. * float(i) + 1.) / pow(2., layerIndex+1.);
        ret += kantan(x, y, ix, iy, slope);
        slope *= -1.;
    }
    if (ret > 0.0) {
        return 1.0;
    } else {
        return 0.0;
    }
}

void main()
{
    float layerIndex = floor(postNum * (1.0 - textureCoord.y));

    //float h = abs(sin(textureCoord.x * PI*pow(2., layerIndex)));
    //h = step(1.0 - EPSIRON, h);
    float h = judge(textureCoord.x, 1.0 - textureCoord.y, layerIndex);
    float dist = distance(textureCoord.xy, vec2(0.5, 1.0));
    float g = 0.5 * sin(3. * uTime + dist) + 0.5;
    float b = 0.5 * cos(3. * uTime + dist) + 0.5;
    //h *= 1.0 - step(uTime, 1.0 - textureCoord.y);
    h *= 1.0 - step(uTime, dist / sqrt(1.25));
    if (h > 0.0) {
        fragColor = vec4(0.5, 0.2 * h*g + 0.8, 0.3 * h*b + 0.7, 1.);
    } else {
        fragColor = vec4(1., 1, 1., 1.);
    }
    //fragColor.r = textureCoord.x;
}