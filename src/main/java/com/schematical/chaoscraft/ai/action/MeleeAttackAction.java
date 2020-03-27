package com.schematical.chaoscraft.ai.action;

import com.schematical.chaoscraft.entities.OrgEntity;
import com.schematical.chaoscraft.util.ChaosTarget;

public class MeleeAttackAction extends NavigateToAction{

    @Override
    protected void _tick() {
        tickLook();
        if(
            !getTarget().canEntityTouch(getOrgEntity()) &&
            !getTarget().isEntityLookingAt(getOrgEntity())
        ){
            tickNavigate();
            return;
        }
        stopWalking();
        //When looking at stuff do stuff.
        getOrgEntity().attackEntityAsMob(getTarget().getTargetEntity());
        if(!getTarget().getTargetEntity().isAlive()){
            markCompleted();
        }
    }


    public static boolean validateTarget(OrgEntity orgEntity, ChaosTarget chaosTarget) {
        return chaosTarget.getTargetEntity() != null;
    }
}
