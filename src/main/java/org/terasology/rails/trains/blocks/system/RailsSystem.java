/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rails.trains.blocks.system;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;
import org.terasology.rails.minecarts.components.WrenchComponent;
import org.terasology.rails.trains.blocks.components.TrainRailComponent;
import org.terasology.rails.trains.blocks.system.Builder.Builder;
import org.terasology.rails.trains.blocks.system.Misc.Orientation;
import org.terasology.rails.trains.components.RailBuilderComponent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import javax.vecmath.Vector3f;

@RegisterSystem
public class RailsSystem extends BaseComponentSystem {
    public static final float TRACK_LENGTH = 1f;
    public static final float STANDARD_ANGLE_CHANGE = 7.5f;
    public static final float STANDARD_PITCH_ANGLE_CHANGE = 7.5f;

    @In
    private BlockManager blockManager;
    @In
    private EntityManager entityManager;

    private final Logger logger = LoggerFactory.getLogger(RailsSystem.class);
    private Builder railBuilder;

    @ReceiveEvent(components = {RailBuilderComponent.class, ItemComponent.class})
    public void onPlaceFunctional(ActivateEvent event, EntityRef item) {

        float yaw = 0;
        Vector3f placementPos = null;
        boolean reverse = false;
        EntityRef selectedTrack = EntityRef.NULL;

        if (railBuilder == null) {
            railBuilder = new Builder(entityManager);
        }

        if (item.hasComponent(WrenchComponent.class)) {
            return;
        }

        EntityRef targetEntity = event.getTarget();
        BlockComponent blockComponent = targetEntity.getComponent(BlockComponent.class);

        logger.info("1");
        if (blockComponent == null) {
            if (!checkSelectRail(targetEntity, item)) {
                logger.info("2");
                return;
            }

            logger.info("3");
            selectedTrack = targetEntity;

            TrainRailComponent trainRailComponent = selectedTrack.getComponent(TrainRailComponent.class);

            Vector3f  hitPosition = event.getHitPosition();
            if (hitPosition != null) {
                logger.info("GO!");
                Vector3f startPosition = new Vector3f(trainRailComponent.startPosition);
                Vector3f endPosition = new Vector3f(trainRailComponent.endPosition);
                startPosition.sub(hitPosition);
                endPosition.sub(hitPosition);
                float distFromStart = startPosition.lengthSquared();
                float distFromend = endPosition.lengthSquared();

                logger.info("from start:" + distFromStart);
                logger.info("from end:" + distFromend);
                if ( distFromStart > distFromend && trainRailComponent.prevTrack == null) {
                    reverse = true;
                }
                logger.info("4");
            }
        }

        RailBuilderComponent railBuilderComponent = item.getComponent(RailBuilderComponent.class);

        if (selectedTrack.equals(EntityRef.NULL)) {
            placementPos = new Vector3i(event.getTarget().getComponent(BlockComponent.class).getPosition()).toVector3f();
            placementPos.y += 0.65f;

            Vector3f direction = event.getDirection();
            direction.y = 0;
            Direction dir = Direction.inDirection(direction);


            switch (dir) {
                case LEFT:
                    yaw = 90;
                    placementPos.x -=0.5f;
                    logger.info("LEFT");
                    break;
                case RIGHT:
                    yaw = 270;
                    placementPos.x +=0.5f;
                    logger.info("RIGHT");
                    break;
                case FORWARD:
                    yaw = 0;
                    placementPos.z -=0.5f;
                    logger.info("FORWARD");
                    break;
                case BACKWARD:
                    logger.info("BACKWARD");
                    placementPos.z +=0.5f;
                    yaw = 180;
                    break;
            }
        } else {
            logger.info("Track is selected!");
        }

        switch (railBuilderComponent.type) {
            case LEFT:
                railBuilder.buildLeft(placementPos, selectedTrack, new Orientation(yaw, 0, 0), reverse);
                break;
            case RIGHT:
                railBuilder.buildRight(placementPos, selectedTrack, new Orientation(yaw, 0, 0), reverse);
                break;
            case UP:
                railBuilder.buildUp(placementPos, selectedTrack, new Orientation(yaw, 0, 0), reverse);
                break;
            case DOWN:
                railBuilder.buildDown(placementPos, selectedTrack, new Orientation(yaw, 0, 0), reverse);
                break;
            case STRAIGHT:
                logger.info("buildStraight!");
                railBuilder.buildStraight(placementPos, selectedTrack, new Orientation(yaw, 0, 0), reverse);
                break;
        }

        event.consume();
    }

    private boolean checkSelectRail(EntityRef target, EntityRef item) {
        if (!target.hasComponent(TrainRailComponent.class)) {
            return false;
        }

        return true;
    }
}
