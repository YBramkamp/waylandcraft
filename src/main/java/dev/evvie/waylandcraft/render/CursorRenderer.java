package dev.evvie.waylandcraft.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.evvie.waylandcraft.WaylandCraft;
import dev.evvie.waylandcraft.WaylandCraftCommon;
import dev.evvie.waylandcraft.bridge.IconSurface;
import dev.evvie.waylandcraft.bridge.WLCAbstractWindow;
import dev.evvie.waylandcraft.bridge.WLCSurface;
import dev.evvie.waylandcraft.utils.CursorShape;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class CursorRenderer {

	private static final Identifier DEFAULT_CURSOR = Identifier.fromNamespaceAndPath(WaylandCraftCommon.MOD_ID, "textures/gui/sprites/crosshair/pointer.png");
	private static final double CURSOR_DEPTH = 0.018;
	private static final double DEFAULT_CURSOR_SIZE = 15.0;

	private CursorRenderer() {
	}

	public static void renderWorldCursor(PoseStack poseStack, SubmitNodeCollector collector, Vec3 localX, Vec3 localY, Vec3 normal, WLCAbstractWindow window) {
		CursorPlacement placement = placementForCapturedWindow(window);
		if(placement == null) return;

		Vec3 depth = normal.scale(CURSOR_DEPTH);
		IconSurface cursor = WaylandCraft.instance.bridge.cursorIcon;
		if(cursor != null && cursor.framebuffer != null && cursor.framebuffer.isValid()) {
			WindowFramebuffer framebuffer = cursor.framebuffer;
			double x = placement.x - WaylandCraft.instance.bridge.cursorHotspotX - framebuffer.getXOff();
			double y = placement.y - WaylandCraft.instance.bridge.cursorHotspotY - framebuffer.getYOff();
			Vec3 origin = localX.scale(x).add(localY.scale(y)).add(depth);
			RenderUtils.renderFramebuffer(framebuffer, poseStack, collector, true, origin, localX.scale(framebuffer.getWidth()), localY.scale(framebuffer.getHeight()));
			return;
		}

		if(WaylandCraft.instance.cursorShape == CursorShape.HIDE) return;
		Vec3 origin = localX.scale(placement.x).add(localY.scale(placement.y)).add(depth);
		RenderUtils.renderTexture(poseStack, collector, DEFAULT_CURSOR, origin, localX.scale(DEFAULT_CURSOR_SIZE), localY.scale(DEFAULT_CURSOR_SIZE));
	}

	public static void renderScreenCursor(GuiGraphicsExtractor context, WLCAbstractWindow window, double originX, double originY, double scale, @Nullable WLCSurface surface, double surfaceX, double surfaceY) {
		if(surface == null || !surfaceInTree(window.getSurfaceTree(), surface)) return;

		double pointerX = originX + (surface.xSubpos + surfaceX) * scale;
		double pointerY = originY + (surface.ySubpos + surfaceY) * scale;

		IconSurface cursor = WaylandCraft.instance.bridge.cursorIcon;
		if(cursor != null && cursor.framebuffer != null && cursor.framebuffer.isValid()) {
			WindowFramebuffer framebuffer = cursor.framebuffer;
			double x = pointerX - (WaylandCraft.instance.bridge.cursorHotspotX + framebuffer.getXOff()) * scale;
			double y = pointerY - (WaylandCraft.instance.bridge.cursorHotspotY + framebuffer.getYOff()) * scale;
			RenderUtils.renderFramebuffer2D(context, framebuffer, (int) Math.round(x), (int) Math.round(y), (int) Math.round(framebuffer.getWidth() * scale), (int) Math.round(framebuffer.getHeight() * scale));
			return;
		}

		if(WaylandCraft.instance.cursorShape == CursorShape.HIDE) return;
		double size = DEFAULT_CURSOR_SIZE * scale;
		RenderUtils.renderTexture2D(context, DEFAULT_CURSOR, pointerX, pointerY, size, size);
	}

	private static @Nullable CursorPlacement placementForCapturedWindow(WLCAbstractWindow window) {
		WaylandCraft wlc = WaylandCraft.instance;
		if(wlc == null || wlc.bridge == null || wlc.pointerCapture == null || !wlc.pointerCapture.hard) return null;
		if(window.getSurfaceTree() == null || !surfaceInTree(window.getSurfaceTree(), wlc.pointerCapture.surface)) return null;

		return new CursorPlacement(
				wlc.pointerCapture.surface.xSubpos + wlc.pointerCapture.x - window.geometry.x(),
				wlc.pointerCapture.surface.ySubpos + wlc.pointerCapture.y - window.geometry.y());
	}

	private static boolean surfaceInTree(@Nullable WLCSurface root, WLCSurface target) {
		for(WLCSurface surface = root; surface != null; surface = surface.getNextChild()) {
			if(surface == target) return true;
		}
		return false;
	}

	private static record CursorPlacement(double x, double y) {
	}

}
