package net.fodoth.skina.neoguanniao.client.camera;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** Shared low-level drawing primitive for all flat camera assets. */
final class CameraRenderUtil {
    private CameraRenderUtil() {
    }

    static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                       float u, float v, int packedLight) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setLight(packedLight);
    }
}
