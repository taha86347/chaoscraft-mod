package com.schematical.chaoscraft.services.targetnet;

import com.schematical.chaoscraft.ChaosCraft;
import com.schematical.chaoscraft.ai.NeuralNet;
import com.schematical.chaoscraft.ai.OutputNeuron;
import com.schematical.chaoscraft.ai.biology.ActionTargetSlot;
import com.schematical.chaoscraft.client.ClientOrgManager;
import com.schematical.chaosnet.model.ChaosNetException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ScanItemInstance {



    private  State state = State.Pending;
    private ClientOrgManager clientOrgManager;
    private HashMap<String, ScanResult> highestResults = new HashMap<>();
    private ScanEntry focusedScanEntity;

    public ScanItemInstance(ClientOrgManager clientOrgManager){
        this.clientOrgManager = clientOrgManager;

    }
    public State getState(){
        return state;
    }



    public void scan(){
        if(!state.equals(State.Pending)){
            throw new ChaosNetException("Invalid State: " + state);
        }



        ArrayList<IRecipe> recipes = clientOrgManager.getEntity().getAllCraftableRecipes();
        for (IRecipe recipe : recipes) {
            focusedScanEntity = new ScanEntry();
            focusedScanEntity.recipe = recipe;
            List<OutputNeuron> outputs = clientOrgManager.getNNet().evaluate(NeuralNet.EvalGroup.ITEM);//Ideally the output neurons will set the score

            Iterator<OutputNeuron> iterator = outputs.iterator();

            while (iterator.hasNext()) {
                OutputNeuron outputNeuron = iterator.next();
                outputNeuron.execute();
            }
            HashMap<String, Float> scores = focusedScanEntity.getScores();
            for (String targetSlotId : scores.keySet()) {
                if (!highestResults.containsKey(targetSlotId)) {
                    highestResults.put(targetSlotId, new ScanResult(targetSlotId));
                }
                ScanResult scanResult = highestResults.get(targetSlotId);
                scanResult.test(focusedScanEntity);
            }
        }


        ItemStackHandler itemStackHandler = clientOrgManager.getEntity().getItemHandler();

        boolean hasSearchedEmpty = false;

        for(int i = 0; i < itemStackHandler.getSlots(); i++){
            focusedScanEntity = new ScanEntry();
            focusedScanEntity.targetSlot = i;
            ItemStack itemStack = itemStackHandler.getStackInSlot(focusedScanEntity.targetSlot);
            if(
                !itemStack.isEmpty() ||
                !hasSearchedEmpty
            ) {
                List<OutputNeuron> outputs = clientOrgManager.getNNet().evaluate(NeuralNet.EvalGroup.ITEM);

                Iterator<OutputNeuron> iterator = outputs.iterator();

                while (iterator.hasNext()) {
                    OutputNeuron outputNeuron = iterator.next();
                    outputNeuron.execute();
                }
                HashMap<String, Float> scores = focusedScanEntity.getScores();
                for (String targetSlotId : scores.keySet()) {
                    if (!highestResults.containsKey(targetSlotId)) {
                        highestResults.put(targetSlotId, new ScanResult(targetSlotId));
                    }
                    ScanResult scanResult = highestResults.get(targetSlotId);
                    scanResult.test(focusedScanEntity);
                }
                if(itemStack.isEmpty()){
                    hasSearchedEmpty = true;
                }else{
                    //ChaosCraft.LOGGER.debug("DELETE ME HERE IM JUST HERE FOR A DEBUG POINT");
                }
            }

        }

        state = State.Finished;

    }
    public ScanEntry getFocusedScanEntry() {
        return focusedScanEntity;
    }

    public  HashMap<String, ScanResult> getScanResults() {
        return highestResults;
    }


    public enum State{
        Pending,
        Finished
    }
}
