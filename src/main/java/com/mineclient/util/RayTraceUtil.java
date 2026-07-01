package com.mineclient.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

public final class RayTraceUtil {

    private RayTraceUtil() {
    }

    /**
     * Recalcula {@code mc.objectMouseOver}/{@code mc.pointedEntity} com alcance
     * customizado, espelhando a lógica de EntityRenderer#getMouseOver. Assim o
     * clique de ataque (Minecraft#clickMouse) enxerga entidades além dos 3
     * blocos padrão. O servidor integrado (singleplayer/LAN) aceita ataques
     * até 6 blocos de distância — acima disso o hit é descartado por ele.
     *
     * @param reach        alcance desejado em blocos
     * @param hitboxExpand expansão extra da hitbox de cada entidade (em blocos)
     */
    public static void updateMouseOver(float partialTicks, double reach, double hitboxExpand) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        if (viewer == null || mc.theWorld == null) {
            return;
        }

        Vec3 eyes = viewer.getPositionEyes(partialTicks);
        MovingObjectPosition blockHit = viewer.rayTrace(reach, partialTicks);

        double entityRange = reach;
        if (blockHit != null) {
            entityRange = blockHit.hitVec.distanceTo(eyes);
        }

        Vec3 look = viewer.getLook(partialTicks);
        Vec3 end = eyes.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);

        AxisAlignedBB searchBox = viewer.getEntityBoundingBox()
                .addCoord(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach)
                .expand(1.0D, 1.0D, 1.0D);

        List<Entity> candidates = mc.theWorld.getEntitiesInAABBexcluding(viewer, searchBox,
                Predicates.<Entity>and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
                    @Override
                    public boolean apply(Entity input) {
                        return input != null && input.canBeCollidedWith();
                    }
                }));

        Entity pointed = null;
        Vec3 hitVec = null;
        double closest = entityRange;

        for (Entity candidate : candidates) {
            float border = candidate.getCollisionBorderSize() + (float) hitboxExpand;
            AxisAlignedBB aabb = candidate.getEntityBoundingBox().expand(border, border, border);
            MovingObjectPosition intercept = aabb.calculateIntercept(eyes, end);

            if (aabb.isVecInside(eyes)) {
                if (closest >= 0.0D) {
                    pointed = candidate;
                    hitVec = intercept == null ? eyes : intercept.hitVec;
                    closest = 0.0D;
                }
            } else if (intercept != null) {
                double distance = eyes.distanceTo(intercept.hitVec);
                if (distance < closest || closest == 0.0D) {
                    if (candidate == viewer.ridingEntity && !viewer.canRiderInteract()) {
                        if (closest == 0.0D) {
                            pointed = candidate;
                            hitVec = intercept.hitVec;
                        }
                    } else {
                        pointed = candidate;
                        hitVec = intercept.hitVec;
                        closest = distance;
                    }
                }
            }
        }

        if (pointed != null && (closest < entityRange || blockHit == null)) {
            mc.objectMouseOver = new MovingObjectPosition(pointed, hitVec);
            mc.pointedEntity = pointed;
        }
    }
}
