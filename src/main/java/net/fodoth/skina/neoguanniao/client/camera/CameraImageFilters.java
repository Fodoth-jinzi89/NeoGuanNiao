package net.fodoth.skina.neoguanniao.client.camera;
import net.fodoth.skina.neoguanniao.content.camera.CameraFilter;

import net.minecraft.util.Mth;

final class CameraImageFilters {
        private static final float SHARPEN_STRENGTH = 0.16f;

    private CameraImageFilters() {
    }

        static void apply(int[] pixels, int width, int height, CameraFilter filter, long seed) {
        if (filter == CameraFilter.COLOR_BALANCE) {
            CameraImageFilters.autoColorBalance(pixels);
        } else if (filter != CameraFilter.NONE) {
            CameraImageFilters.applyUniversal(pixels, width, height, filter.id(), seed);
        }
        CameraImageFilters.sharpen(pixels, width, height);
    }

    private static void applyUniversal(int[] pixels, int width, int height, int mode, long seed) {
        boolean samplesNeighbours = mode == CameraFilter.GLITCH_RGB.id() || mode == CameraFilter.CHROMATIC_ABERRATION.id();
        int[] source = samplesNeighbours ? (int[])pixels.clone() : pixels;
        for (int index = 0; index < pixels.length; ++index) {
            int x = index % width;
            int yPixel = index / width;
            int pixel = source[index];
            double u = ((double)x + 0.5) / (double)width;
            double v = ((double)yPixel + 0.5) / (double)height;
            double red = CameraImageFilters.normalized(CameraImageFilters.red(pixel));
            double green = CameraImageFilters.normalized(CameraImageFilters.green(pixel));
            double blue = CameraImageFilters.normalized(CameraImageFilters.blue(pixel));
            double sourceRed = red;
            double sourceGreen = green;
            double sourceBlue = blue;
            double luminance = CameraImageFilters.luminance(red, green, blue);
            switch (mode) {
                case 1: {
                    double grey;
                    green = blue = (grey = CameraImageFilters.contrast(luminance, 1.18));
                    red = blue;
                    break;
                }
                case 2: {
                    double tintedLuminance = CameraImageFilters.luminance(red *= 1.03, green, blue *= 0.95);
                    red = CameraImageFilters.saturate(red, tintedLuminance, 0.95);
                    green = CameraImageFilters.saturate(green, tintedLuminance, 0.95);
                    blue = CameraImageFilters.saturate(blue, tintedLuminance, 0.95);
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.075);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.3, 0.66, 0.22);
                    red = (red + grain) * amount;
                    green = (green + grain) * amount;
                    blue = (blue + grain) * amount;
                    break;
                }
                case 3: {
                    red = CameraImageFilters.expose(red, 1.52);
                    green = CameraImageFilters.expose(green, 1.52);
                    blue = CameraImageFilters.expose(blue, 1.52);
                    double exposedLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, exposedLuminance, 1.08);
                    green = CameraImageFilters.saturate(green, exposedLuminance, 1.08);
                    blue = CameraImageFilters.saturate(blue, exposedLuminance, 1.08);
                    break;
                }
                case 5: {
                    double grey;
                    green = blue = (grey = CameraImageFilters.smoothstep(0.025, 0.975, CameraImageFilters.contrast(luminance, 1.7)));
                    red = blue;
                    break;
                }
                case 6: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 1.9), 1.25);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 1.9), 1.25);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 1.9), 1.25);
                    break;
                }
                case 7: {
                    double tintedLuminance = CameraImageFilters.luminance(red *= 1.18, green *= 1.04, blue *= 0.76);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, tintedLuminance, 0.82), 1.07) * 0.88 + 0.075;
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, tintedLuminance, 0.82), 1.07) * 0.88 + 0.052;
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, tintedLuminance, 0.82), 1.07) * 0.88 + 0.025;
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.31, 0.68, 0.28);
                    red *= amount;
                    green *= amount;
                    blue *= amount;
                    break;
                }
                case 8: {
                    double amount = CameraImageFilters.smoothstep(0.26, 0.8, luminance);
                    red = CameraImageFilters.lerp(amount, red * 0.68, red * 1.23);
                    green = CameraImageFilters.lerp(amount, green * 1.04, green * 1.01);
                    blue = CameraImageFilters.lerp(amount, blue * 1.22, blue * 0.68);
                    double mixedLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, mixedLuminance, 1.23), 1.18);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, mixedLuminance, 1.23), 1.18);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, mixedLuminance, 1.23), 1.18);
                    break;
                }
                case 9: {
                    red = CameraImageFilters.expose(red, 1.48) * 1.0576;
                    green = CameraImageFilters.expose(green, 1.48) * 0.9856;
                    blue = CameraImageFilters.expose(blue, 1.48) * 1.0792;
                    double amount = CameraImageFilters.smoothstep(0.55, 1.0, CameraImageFilters.luminance(red, green, blue)) * 0.22;
                    red = CameraImageFilters.lerp(amount, red, 1.0);
                    green = CameraImageFilters.lerp(amount, green, 0.94);
                    blue = CameraImageFilters.lerp(amount, blue, 0.985);
                    double dreamyLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, dreamyLuminance, 0.9);
                    green = CameraImageFilters.saturate(green, dreamyLuminance, 0.9);
                    blue = CameraImageFilters.saturate(blue, dreamyLuminance, 0.9);
                    break;
                }
                case 10: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 1.75), 1.3) * 1.06;
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 1.75), 1.3) * 0.98;
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 1.75), 1.3) * 1.02;
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.26, 0.62, 0.58);
                    red *= amount;
                    green *= amount;
                    blue *= amount;
                    break;
                }
                case 11: {
                    red = CameraImageFilters.lerp(0.48, red, CameraImageFilters.expose(red, 1.35));
                    green = CameraImageFilters.lerp(0.48, green, CameraImageFilters.expose(green, 1.35));
                    blue = CameraImageFilters.lerp(0.48, blue, CameraImageFilters.expose(blue, 1.35));
                    double softLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, softLuminance, 0.92), 0.92);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, softLuminance, 0.92), 0.92);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, softLuminance, 0.92), 0.92);
                    break;
                }
                case 12: {
                    red = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 1.12), 1.52), 1.05);
                    green = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 1.12), 1.52), 1.05);
                    blue = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 1.12), 1.52), 1.05);
                    break;
                }
                case 13: {
                    red = CameraImageFilters.expose(red, 1.3) * 1.16;
                    green = CameraImageFilters.expose(green, 1.3) * 1.03;
                    blue = CameraImageFilters.expose(blue, 1.3) * 0.83;
                    double warmLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, warmLuminance, 1.12);
                    green = CameraImageFilters.saturate(green, warmLuminance, 1.12);
                    blue = CameraImageFilters.saturate(blue, warmLuminance, 1.12);
                    break;
                }
                case 14: {
                    red = CameraImageFilters.contrast(red * 0.88, 1.17);
                    green = CameraImageFilters.contrast(green * 1.01, 1.17);
                    blue = CameraImageFilters.contrast(blue * 1.18, 1.17);
                    double coolLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, coolLuminance, 1.13);
                    green = CameraImageFilters.saturate(green, coolLuminance, 1.13);
                    blue = CameraImageFilters.saturate(blue, coolLuminance, 1.13);
                    break;
                }
                case 15: {
                    double mask = CameraImageFilters.smoothstep(0.02, 0.35, blue - Math.max(red, green) * 0.72) * 0.82;
                    red = CameraImageFilters.lerp(mask, red, red * 0.92);
                    green = CameraImageFilters.lerp(mask, green, green * 1.05);
                    blue = CameraImageFilters.lerp(mask, blue, blue * 1.38);
                    double skyLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, skyLuminance, 1.2);
                    green = CameraImageFilters.saturate(green, skyLuminance, 1.2);
                    blue = CameraImageFilters.saturate(blue, skyLuminance, 1.2);
                    break;
                }
                case 16: {
                    double mask = CameraImageFilters.smoothstep(0.0, 0.3, green - Math.max(red, blue) * 0.78) * 0.75;
                    red = CameraImageFilters.lerp(mask, red, red * 0.9);
                    green = CameraImageFilters.lerp(mask, green, green * 1.3);
                    blue = CameraImageFilters.lerp(mask, blue, blue * 0.89);
                    double forestLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, forestLuminance, 1.2), 1.1);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, forestLuminance, 1.2), 1.1);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, forestLuminance, 1.2), 1.1);
                    break;
                }
                case 17: {
                    red *= 1.27;
                    green *= 0.96;
                    blue *= 0.72;
                    double amount = CameraImageFilters.smoothstep(0.25, 0.75, luminance);
                    green = CameraImageFilters.lerp(amount, green, green * 0.86);
                    blue = CameraImageFilters.lerp(amount, blue, blue * 1.05);
                    double sunsetLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, sunsetLuminance, 1.28), 1.12);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, sunsetLuminance, 1.28), 1.12);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, sunsetLuminance, 1.28), 1.12);
                    break;
                }
                case 18: {
                    red = red * 0.88 * 0.83 + 0.055;
                    green = green * 1.02 * 0.83 + 0.066;
                    blue = blue * 1.13 * 0.83 + 0.082;
                    double vintageLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, vintageLuminance, 0.78);
                    green = CameraImageFilters.saturate(green, vintageLuminance, 0.78);
                    blue = CameraImageFilters.saturate(blue, vintageLuminance, 0.78);
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.045);
                    red += grain;
                    green += grain;
                    blue += grain;
                    break;
                }
                case 19: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 0.68), 0.78);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 0.68), 0.78);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 0.68), 0.78);
                    red = CameraImageFilters.lerp(0.1, red, 0.9) * 0.82 + 0.09;
                    green = CameraImageFilters.lerp(0.1, green, 0.83) * 0.82 + 0.09;
                    blue = CameraImageFilters.lerp(0.1, blue, 0.7) * 0.82 + 0.09;
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.035);
                    red += grain;
                    green += grain;
                    blue += grain;
                    break;
                }
                case 20: {
                    red = CameraImageFilters.lerp(0.58, red, luminance);
                    green = CameraImageFilters.lerp(0.58, green, luminance);
                    blue = CameraImageFilters.lerp(0.58, blue, luminance);
                    red = CameraImageFilters.contrast(red, 1.62);
                    green = CameraImageFilters.contrast(green, 1.62);
                    blue = CameraImageFilters.contrast(blue, 1.62);
                    double bleachLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, bleachLuminance, 0.72);
                    green = CameraImageFilters.saturate(green, bleachLuminance, 0.72);
                    blue = CameraImageFilters.saturate(blue, bleachLuminance, 0.72);
                    break;
                }
                case 21: {
                    red = CameraImageFilters.contrast(sourceRed * 0.393 + sourceGreen * 0.769 + sourceBlue * 0.189, 1.16);
                    green = CameraImageFilters.contrast(sourceRed * 0.349 + sourceGreen * 0.686 + sourceBlue * 0.168, 1.16);
                    blue = CameraImageFilters.contrast(sourceRed * 0.272 + sourceGreen * 0.534 + sourceBlue * 0.131, 1.16);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.31, 0.69, 0.25);
                    red *= amount;
                    green *= amount;
                    blue *= amount;
                    break;
                }
                case 22: {
                    double amount = CameraImageFilters.smoothstep(0.36, 0.74, luminance);
                    red = CameraImageFilters.lerp(amount, red * 0.62, red * 1.3);
                    green = CameraImageFilters.lerp(amount, green * 0.86, green * 1.09);
                    blue = CameraImageFilters.lerp(amount, blue * 1.26, blue * 0.62);
                    double cinemaLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, cinemaLuminance, 1.15), 1.18);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, cinemaLuminance, 1.15), 1.18);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, cinemaLuminance, 1.15), 1.18);
                    break;
                }
                case 23: {
                    red = CameraImageFilters.smoothstep(0.02, 0.86, red);
                    green = Math.pow(green, 0.82);
                    blue = Math.pow(blue, 1.23) * 1.12;
                    double crossLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, crossLuminance, 1.35), 1.12);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, crossLuminance, 1.35), 1.12);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, crossLuminance, 1.35), 1.12);
                    break;
                }
                case 24: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 1.48), 1.32) * 1.08;
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 1.48), 1.32) * 0.94;
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 1.48), 1.32) * 1.03;
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.2, 0.58, 0.68);
                    double grain = CameraImageFilters.grainOffset(red *= amount, green *= amount, blue *= amount, x, yPixel, seed, 0.055);
                    red += grain;
                    green += grain;
                    blue += grain;
                    break;
                }
                case 25: {
                    double grey;
                    green = blue = (grey = CameraImageFilters.clamp01((luminance - 0.5) * 0.83 + 0.55));
                    red = blue;
                    break;
                }
                case 26: {
                    double grey;
                    green = blue = (grey = CameraImageFilters.contrast(CameraImageFilters.expose(luminance, 1.7), 1.05));
                    red = blue;
                    break;
                }
                case 27: {
                    double grey = CameraImageFilters.contrast(Math.pow(luminance, 1.55), 1.45);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.27, 0.66, 0.32);
                    green = blue = grey * amount;
                    red = blue;
                    break;
                }
                case 28: {
                    red = CameraImageFilters.contrast(luminance * 1.08, 1.22);
                    green = CameraImageFilters.contrast(luminance, 1.22);
                    blue = CameraImageFilters.contrast(luminance * 0.86, 1.22);
                    break;
                }
                case 29: {
                    red = CameraImageFilters.contrast(luminance * 0.84, 1.24);
                    green = CameraImageFilters.contrast(luminance * 0.96, 1.24);
                    blue = CameraImageFilters.contrast(luminance * 1.13, 1.24);
                    break;
                }
                case 30: {
                    double grey = CameraImageFilters.contrast(luminance, 1.32);
                    red = grey * 0.92;
                    green = grey * 0.98;
                    blue = grey * 1.05;
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.04);
                    red += grain;
                    green += grain;
                    blue += grain;
                    break;
                }
                case 31: {
                    double grey;
                    green = blue = (grey = Math.floor(CameraImageFilters.clamp01(CameraImageFilters.contrast(luminance, 1.85)) * 7.0) / 7.0);
                    red = blue;
                    break;
                }
                case 32: {
                    double grey;
                    green = blue = (grey = CameraImageFilters.contrast(luminance, 1.68));
                    red = blue;
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.105);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.24, 0.62, 0.52);
                    red = (red + grain) * amount;
                    green = (green + grain) * amount;
                    blue = (blue + grain) * amount;
                    break;
                }
                case 33: {
                    red = CameraImageFilters.lerp(0.18, CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 0.72), 0.78), 0.8);
                    green = CameraImageFilters.lerp(0.18, CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 0.72), 0.78), 0.88);
                    blue = CameraImageFilters.lerp(0.18, CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 0.72), 0.78), 0.92);
                    red = CameraImageFilters.expose(red, 1.22);
                    green = CameraImageFilters.expose(green, 1.22);
                    blue = CameraImageFilters.expose(blue, 1.22);
                    break;
                }
                case 34: {
                    red *= 1.17;
                    green *= 0.96;
                    blue *= 1.06;
                    double amount = CameraImageFilters.smoothstep(0.5, 1.0, luminance) * 0.18;
                    red = CameraImageFilters.expose(CameraImageFilters.lerp(amount, red, 1.0), 1.18);
                    green = CameraImageFilters.expose(CameraImageFilters.lerp(amount, green, 0.7), 1.18);
                    blue = CameraImageFilters.expose(CameraImageFilters.lerp(amount, blue, 0.76), 1.18);
                    break;
                }
                case 35: {
                    red = Math.pow(red, 1.18) * 0.64;
                    green = Math.pow(green, 1.18) * 0.82;
                    blue = Math.pow(blue, 1.18) * 1.24;
                    double moonLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, moonLuminance, 0.72), 1.18);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, moonLuminance, 0.72), 1.18);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, moonLuminance, 0.72), 1.18);
                    break;
                }
                case 36: {
                    red = CameraImageFilters.expose(red, 1.27) * 1.16;
                    green = CameraImageFilters.expose(green, 1.27) * 0.91;
                    blue = CameraImageFilters.expose(blue, 1.27) * 1.05;
                    double pinkLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, pinkLuminance, 1.08);
                    green = CameraImageFilters.saturate(green, pinkLuminance, 1.08);
                    blue = CameraImageFilters.saturate(blue, pinkLuminance, 1.08);
                    break;
                }
                case 37: {
                    red = red * 1.2 + sourceGreen * 0.08;
                    green = green * 1.04 * 0.94;
                    double autumnLuminance = CameraImageFilters.luminance(red, green, blue *= 0.72);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, autumnLuminance, 1.3), 1.1);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, autumnLuminance, 1.3), 1.1);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, autumnLuminance, 1.3), 1.1);
                    break;
                }
                case 38: {
                    red = CameraImageFilters.expose(red * 1.06, 1.22);
                    green = CameraImageFilters.expose(green * 1.17, 1.22);
                    blue = CameraImageFilters.expose(blue * 0.98, 1.22);
                    double springLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, springLuminance, 1.16);
                    green = CameraImageFilters.saturate(green, springLuminance, 1.16);
                    blue = CameraImageFilters.saturate(blue, springLuminance, 1.16);
                    break;
                }
                case 39: {
                    double winterLuminance = CameraImageFilters.luminance(red *= 0.82, green *= 0.98, blue *= 1.22);
                    red = CameraImageFilters.expose(CameraImageFilters.saturate(red, winterLuminance, 0.78), 1.15);
                    green = CameraImageFilters.expose(CameraImageFilters.saturate(green, winterLuminance, 0.78), 1.15);
                    blue = CameraImageFilters.expose(CameraImageFilters.saturate(blue, winterLuminance, 0.78), 1.15);
                    break;
                }
                case 40: {
                    double summerLuminance = CameraImageFilters.luminance(red *= 1.13, green *= 1.07, blue *= 0.88);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, summerLuminance, 1.42), 1.14);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, summerLuminance, 1.42), 1.14);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, summerLuminance, 1.42), 1.14);
                    break;
                }
                case 41: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 1.8), 1.32) * 1.16;
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 1.8), 1.32) * 0.78 + CameraImageFilters.smoothstep(0.45, 1.0, sourceBlue) * 0.16;
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 1.8), 1.32) * 1.3;
                    break;
                }
                case 42: {
                    red = CameraImageFilters.contrast(red, 1.38) * 0.66;
                    green = CameraImageFilters.contrast(green, 1.38) * 1.22;
                    blue = CameraImageFilters.contrast(blue, 1.38) * 0.6;
                    double horrorLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.saturate(red, horrorLuminance, 0.72);
                    green = CameraImageFilters.saturate(green, horrorLuminance, 0.72);
                    blue = CameraImageFilters.saturate(blue, horrorLuminance, 0.72);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.25, 0.62, 0.52);
                    red *= amount;
                    green *= amount;
                    blue *= amount;
                    break;
                }
                case 43: {
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 0.58) * 1.26, 1.42);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 0.58) * 0.82, 1.42);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 0.58) * 0.53, 1.42);
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.06);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.29, 0.66, 0.3);
                    red = (red + grain) * amount;
                    green = (green + grain) * amount;
                    blue = (blue + grain) * amount;
                    break;
                }
                case 44: {
                    int band = (int)Math.floor(v * 90.0);
                    double kick = CameraImageFilters.noise01(band, 0, seed) >= 0.84 ? 1.0 : 0.0;
                    int shift = (int)Math.round((CameraImageFilters.noise01(band * 23, 7, seed ^ 0x5DEECE66DL) - 0.5) * 0.045 * kick * (double)width);
                    int colorOffset = Math.max(1, (int)Math.round((double)width * 0.004));
                    red = CameraImageFilters.sampleChannel(source, width, height, x + shift + colorOffset, yPixel, 0);
                    green = CameraImageFilters.sampleChannel(source, width, height, x + shift, yPixel, 1);
                    blue = CameraImageFilters.sampleChannel(source, width, height, x + shift - colorOffset, yPixel, 2);
                    double glitchLuminance = CameraImageFilters.luminance(red, green, blue);
                    red = CameraImageFilters.contrast(CameraImageFilters.saturate(red, glitchLuminance, 1.35), 1.18);
                    green = CameraImageFilters.contrast(CameraImageFilters.saturate(green, glitchLuminance, 1.35), 1.18);
                    blue = CameraImageFilters.contrast(CameraImageFilters.saturate(blue, glitchLuminance, 1.35), 1.18);
                    break;
                }
                case 45: {
                    int offsetX = (int)Math.round((u - 0.5) * 0.018 * (double)width);
                    int offsetY = (int)Math.round((v - 0.5) * 0.018 * (double)height);
                    red = CameraImageFilters.sampleChannel(source, width, height, x + offsetX, yPixel + offsetY, 0);
                    green = sourceGreen;
                    blue = CameraImageFilters.sampleChannel(source, width, height, x - offsetX, yPixel - offsetY, 2);
                    red = CameraImageFilters.contrast(red, 1.12);
                    green = CameraImageFilters.contrast(green, 1.12);
                    blue = CameraImageFilters.contrast(blue, 1.12);
                    break;
                }
                case 46: {
                    red = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(red, luminance, 2.35), 1.48), 0.82) * 1.08;
                    green = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(green, luminance, 2.35), 1.48), 0.82) * 0.92;
                    blue = Math.pow(CameraImageFilters.contrast(CameraImageFilters.saturate(blue, luminance, 2.35), 1.48), 0.82) * 1.18;
                    break;
                }
                case 47: {
                    red = CameraImageFilters.contrast(Math.floor(CameraImageFilters.saturate(red, luminance, 1.45) * 6.0 + 0.5) / 6.0, 1.26);
                    green = CameraImageFilters.contrast(Math.floor(CameraImageFilters.saturate(green, luminance, 1.45) * 6.0 + 0.5) / 6.0, 1.26);
                    blue = CameraImageFilters.contrast(Math.floor(CameraImageFilters.saturate(blue, luminance, 1.45) * 6.0 + 0.5) / 6.0, 1.26);
                    break;
                }
                case 48: {
                    double amount = CameraImageFilters.smoothstep(0.08, 0.93, luminance);
                    red = CameraImageFilters.lerp(amount, 0.015, 0.55);
                    green = CameraImageFilters.lerp(amount, 0.035, 0.88);
                    blue = CameraImageFilters.lerp(amount, 0.13, 1.0);
                    break;
                }
                case 49: {
                    double boosted = CameraImageFilters.expose(luminance, 2.25);
                    red = boosted * 0.25;
                    green = boosted;
                    blue = boosted * 0.18;
                    double grain = CameraImageFilters.grainOffset(red, green, blue, x, yPixel, seed, 0.07);
                    double amount = CameraImageFilters.vignette(u, v, width, height, 0.28, 0.65, 0.48);
                    red = (red + grain) * amount;
                    green = (green + grain) * amount;
                    blue = (blue + grain) * amount;
                    break;
                }
                case 50: {
                    double amount;
                    double grey = CameraImageFilters.contrast(luminance, 1.32);
                    if (grey < 0.25) {
                        amount = grey / 0.25;
                        red = CameraImageFilters.lerp(amount, 0.02, 0.1);
                        green = CameraImageFilters.lerp(amount, 0.0, 0.05);
                        blue = CameraImageFilters.lerp(amount, 0.12, 0.75);
                        break;
                    }
                    if (grey < 0.5) {
                        amount = (grey - 0.25) / 0.25;
                        red = CameraImageFilters.lerp(amount, 0.1, 0.85);
                        green = CameraImageFilters.lerp(amount, 0.05, 0.05);
                        blue = CameraImageFilters.lerp(amount, 0.75, 0.35);
                        break;
                    }
                    if (grey < 0.75) {
                        amount = (grey - 0.5) / 0.25;
                        red = CameraImageFilters.lerp(amount, 0.85, 1.0);
                        green = CameraImageFilters.lerp(amount, 0.05, 0.65);
                        blue = CameraImageFilters.lerp(amount, 0.35, 0.05);
                        break;
                    }
                    amount = (grey - 0.75) / 0.25;
                    red = 1.0;
                    green = CameraImageFilters.lerp(amount, 0.65, 1.0);
                    blue = CameraImageFilters.lerp(amount, 0.05, 0.75);
                    break;
                }
            }
            pixels[index] = CameraImageFilters.abgr(CameraImageFilters.alpha(pixel), CameraImageFilters.channel(red), CameraImageFilters.channel(green), CameraImageFilters.channel(blue));
        }
    }

    private static void autoColorBalance(int[] pixels) {
        long redTotal = 0L;
        long greenTotal = 0L;
        long blueTotal = 0L;
        for (int pixel : pixels) {
            redTotal += (long)CameraImageFilters.red(pixel);
            greenTotal += (long)CameraImageFilters.green(pixel);
            blueTotal += (long)CameraImageFilters.blue(pixel);
        }
        double count = Math.max(1, pixels.length);
        double redAverage = (double)redTotal / count;
        double greenAverage = (double)greenTotal / count;
        double blueAverage = (double)blueTotal / count;
        double neutral = (redAverage + greenAverage + blueAverage) / 3.0;
        double redScale = CameraImageFilters.balancedScale(neutral, redAverage);
        double greenScale = CameraImageFilters.balancedScale(neutral, greenAverage);
        double blueScale = CameraImageFilters.balancedScale(neutral, blueAverage);
        for (int index = 0; index < pixels.length; ++index) {
            int pixel = pixels[index];
            pixels[index] = CameraImageFilters.abgr(CameraImageFilters.alpha(pixel), (int)Math.round((double)CameraImageFilters.red(pixel) * redScale), (int)Math.round((double)CameraImageFilters.green(pixel) * greenScale), (int)Math.round((double)CameraImageFilters.blue(pixel) * blueScale));
        }
    }

    private static double balancedScale(double neutral, double channelAverage) {
        if (channelAverage < 1.0) {
            return 1.0;
        }
        double correction = Mth.clamp((double)(neutral / channelAverage), (double)0.82, (double)1.18);
        return CameraImageFilters.lerp(0.48, 1.0, correction);
    }

    private static double sampleChannel(int[] pixels, int width, int height, int x, int y, int channel) {
        int clampedX = Mth.clamp((int)x, (int)0, (int)(width - 1));
        int clampedY = Mth.clamp((int)y, (int)0, (int)(height - 1));
        int pixel = pixels[clampedY * width + clampedX];
        return CameraImageFilters.normalized(switch (channel) {
            case 0 -> CameraImageFilters.red(pixel);
            case 1 -> CameraImageFilters.green(pixel);
            default -> CameraImageFilters.blue(pixel);
        });
    }

    private static double grainOffset(double red, double green, double blue, int x, int y, long seed, double amount) {
        double midtone = 1.0 - Math.abs(CameraImageFilters.luminance(red, green, blue) * 2.0 - 1.0);
        return (CameraImageFilters.noise01(x, y, seed) - 0.5) * amount * (0.35 + 0.65 * midtone);
    }

    private static double noise01(int x, int y, long seed) {
        long hash = seed ^ (long)x * 7146057691288625177L ^ (long)y * -7046029254386353131L;
        hash ^= hash >>> 30;
        hash *= -4658895280553007687L;
        hash ^= hash >>> 27;
        hash *= -7723592293110705685L;
        hash ^= hash >>> 31;
        return (double)(hash >>> 11) * (double)1.110223E-16f;
    }

    private static double vignette(double u, double v, int width, int height, double start, double end, double amount) {
        double aspectY = (double)height / (double)Math.max(1, width);
        double x = u - 0.5;
        double y = (v - 0.5) * aspectY;
        double distance = Math.sqrt(x * x + y * y);
        return 1.0 - CameraImageFilters.smoothstep(start, end, distance) * amount;
    }

    private static double luminance(double red, double green, double blue) {
        return red * 0.2126 + green * 0.7152 + blue * 0.0722;
    }

    private static double saturate(double channel, double luminance, double amount) {
        return CameraImageFilters.clamp01(CameraImageFilters.lerp(amount, luminance, channel));
    }

    private static double contrast(double channel, double amount) {
        return CameraImageFilters.clamp01((channel - 0.5) * amount + 0.5);
    }

    private static double expose(double channel, double power) {
        return 1.0 - Math.pow(1.0 - CameraImageFilters.clamp01(channel), power);
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double progress = CameraImageFilters.clamp01((value - edge0) / (edge1 - edge0));
        return progress * progress * (3.0 - 2.0 * progress);
    }

    private static double lerp(double amount, double start, double end) {
        return start + amount * (end - start);
    }

    private static double normalized(int channel) {
        return (double)channel / 255.0;
    }

    private static int channel(double normalized) {
        return CameraImageFilters.clamp((int)Math.round(CameraImageFilters.clamp01(normalized) * 255.0));
    }

    private static double clamp01(double value) {
        return Mth.clamp((double)value, (double)0.0, (double)1.0);
    }

    private static void sharpen(int[] pixels, int width, int height) {
        if (width < 3 || height < 3) {
            return;
        }
        int[] source = (int[])pixels.clone();
        for (int y = 1; y < height - 1; ++y) {
            for (int x = 1; x < width - 1; ++x) {
                int index = y * width + x;
                int center = source[index];
                int left = source[index - 1];
                int right = source[index + 1];
                int up = source[index - width];
                int down = source[index + width];
                pixels[index] = CameraImageFilters.abgr(CameraImageFilters.alpha(center), CameraImageFilters.sharpenChannel(CameraImageFilters.red(center), CameraImageFilters.red(left), CameraImageFilters.red(right), CameraImageFilters.red(up), CameraImageFilters.red(down)), CameraImageFilters.sharpenChannel(CameraImageFilters.green(center), CameraImageFilters.green(left), CameraImageFilters.green(right), CameraImageFilters.green(up), CameraImageFilters.green(down)), CameraImageFilters.sharpenChannel(CameraImageFilters.blue(center), CameraImageFilters.blue(left), CameraImageFilters.blue(right), CameraImageFilters.blue(up), CameraImageFilters.blue(down)));
            }
        }
    }

    private static int sharpenChannel(int center, int left, int right, int up, int down) {
        float neighborAverage = (float)(left + right + up + down) * 0.25f;
        return CameraImageFilters.clamp(Math.round((float)center + ((float)center - neighborAverage) * SHARPEN_STRENGTH));
    }

    private static int alpha(int abgr) {
        return abgr >>> 24 & 0xFF;
    }

    private static int red(int abgr) {
        return abgr & 0xFF;
    }

    private static int green(int abgr) {
        return abgr >>> 8 & 0xFF;
    }

    private static int blue(int abgr) {
        return abgr >>> 16 & 0xFF;
    }

    private static int abgr(int alpha, int red, int green, int blue) {
        return CameraImageFilters.clamp(alpha) << 24 | CameraImageFilters.clamp(blue) << 16 | CameraImageFilters.clamp(green) << 8 | CameraImageFilters.clamp(red);
    }

    private static int clamp(int channel) {
        return Mth.clamp((int)channel, (int)0, (int)255);
    }
}

