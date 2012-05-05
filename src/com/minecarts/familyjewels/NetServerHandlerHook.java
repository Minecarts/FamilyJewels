package com.minecarts.familyjewels;

import net.minecraft.server.*;
import org.bukkit.Bukkit;

import java.text.MessageFormat;
import java.util.Arrays;

public class NetServerHandlerHook extends net.minecraft.server.NetServerHandler {
    private EntityPlayer player;
    private final int updateRadius = 2;

    public NetServerHandlerHook(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer player){
        super(minecraftserver,networkmanager,player);
        this.player = player;
    }

    @Override
    public void a(Packet14BlockDig packet) {
        if(packet.e == 0x0 || packet.e == 0x2){ //If starting or finished a dig
             makeBlocksDirtyInRadius(player.world,packet.a,packet.b,packet.c,updateRadius);
        }
        super.a(packet);
    }

    @Override
    public void sendPacket(Packet packet){
        if(packet instanceof Packet51MapChunk){
             Packet51MapChunk dataPacket = (Packet51MapChunk) packet;
             Chunk chunk = this.player.world.getChunkAt(dataPacket.a,dataPacket.b);
             ChunkSection[] chunkSections = chunk.h();
             int dataWritten = 0;

             int i = 0;
             if(dataPacket.f){
                 i = '\uffff';
             }

             for(int j1 = 0; j1 < chunkSections.length; ++j1){
                 if (chunkSections[j1] != null && (!dataPacket.f || !chunkSections[j1].a()) && (i & 1 << j1) != 0) {
                     byte[] tempData = replaceCoveredBlocks(chunk, chunkSections[j1]);
    
                     if(dataWritten + tempData.length > dataPacket.rawData.length){
                         System.out.println("Attempting to write " + tempData.length + " bytes into " + dataPacket.rawData.length + " array but need size of " + dataWritten+tempData.length);
                     }
    
                     System.arraycopy(tempData, 0, dataPacket.rawData, dataWritten, tempData.length);
                     dataWritten += tempData.length;
                 }
             }
        }
        super.sendPacket(packet);
    }//sendPacket()

    //Update the blocks in a radius around the punched block becuase of
    //  the fact that lag can cause some ores not to show up right away thus
    //  a player might miss them, by updating N blocks around it, there's a greater
    //  chance the block will be updated before the player gets to it
    private void makeBlocksDirtyInRadius(World world, int x, int y, int z, int radius){
        for(int a = x-radius; a <= x + radius; a++){
            for(int b = y-radius; b <= y + radius; b++){
                for(int c = z-radius; c <= z + radius; c++){
                    if(a==x && b==y && c==z) continue; //Skip the actual block we're hitting to prevent it from reappearing
                    if(Arrays.binarySearch(FamilyJewels.hiddenBlocks, world.getTypeId(a,b,c)) >= 0){ //Only update blocks that are obfuscated
                        world.notify(a,b,c); //Mark the block as dirty, so it's updated to the client, bypasses antixray check
                    }
                }
            }
        }
    }


    public boolean isBlockTransparent(World world, int x, int y, int z){
        int blockType = world.getTypeId(x,y,z);
        return blockType == 0 || //air
                blockType == 8 || //water
                blockType == 9 || //water
                blockType == 10 || //lava
                blockType == 11 || //lava
                blockType == 20 || //glass block
                blockType == 39 || //mushroom (brown)
                blockType == 40 || //mushroom (red)
                blockType == 44 || //slab
                blockType == 50 || //torch
                blockType == 53 || //wood stiars
                blockType == 55 || //redstone wire
                blockType == 63 || //sign
                blockType == 64 || //wood door
                blockType == 65 || //ladder
                blockType == 66 || //tracks
                blockType == 67 || //cobble stair
                blockType == 68 || //sign
                blockType == 69 || //lever
                blockType == 71 || //Iron doors
                blockType == 72 || //Pressure plates
                blockType == 75 || //redstone torch off
                blockType == 76 || //redstone torch on
                blockType == 77 || // button
                blockType == 85 || //fence
                blockType == 92 || //cake
                blockType == 93 || //diode off
                blockType == 94 || //diode on
                blockType == 95 || //chest
                blockType == 96 || //trap door
                blockType == 101 || //iron bar
                blockType == 102 || //glass pane
                blockType == 108 || //brick star
                blockType == 109 || //stone brick stair
                blockType == 113 || //nether fence
                blockType == 114; //nether stair
    }

    private byte[] replaceCoveredBlocks(Chunk chunk, ChunkSection section){

        /*******WARNING WARNING WARNING
            DO NOT FORGET TO CLONE THE BLOCK DATA FOR THIS SECTION
            OTHERWISE YOU WILL OVERWRITE WORLD DATA WHEN SETTING TO STONE
        WARNING WARNING WARNING*********/
        byte[] blockData = section.g().clone(); //Get the block data for this section

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {

                    int worldX = (chunk.x << 4) + x;
                    int worldY = section.c() + y;
                    int worldZ = (chunk.z << 4) + z;

                    int type = section.a(x, y, z);
                    
                    if(chunk.world.getTypeId(worldX,worldY,worldZ) != type){
                        System.out.println("Block type mismatch " + chunk.world.getTypeId(worldX,worldY,worldZ)  +" vs " + type);
                    }
                    
                    if(Arrays.binarySearch(FamilyJewels.hiddenBlocks, type) >= 0){
                        CHECKTYPE: //Check to see if there is air around the block
                        {
                            if(isBlockTransparent(chunk.world, worldX + 1, worldY, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX - 1, worldY, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, worldY + 1, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, worldY - 1, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, worldY, worldZ + 1)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, worldY, worldZ - 1)) break CHECKTYPE;
                            blockData[y << 8 | z << 4 | x] = 1; //Set it to smooth stone
                        }
                    }
                }
            }
        }
        return blockData;
    }
}
