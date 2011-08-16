package com.minecarts.familyjewels;


import net.minecraft.server.*;

import java.util.Arrays;

public class NetServerHandlerHook extends net.minecraft.server.NetServerHandler {
    private EntityPlayer player;
    public final int[] hiddenBlocks = {14,15,16,21,56,73,74,54};

    public NetServerHandlerHook(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer player){
        super(minecraftserver,networkmanager,player);
        this.player = player;
    }

    @Override
    public void a(Packet14BlockDig packet) {
        if(packet.e == 0){ //If it's starting a dig
             int x = packet.a;
             int y = packet.b;
             int z = packet.c;
             player.world.notify(x + 1, y, z);
             player.world.notify(x - 1, y, z);
             player.world.notify(x, y + 1, z);
             player.world.notify(x, y - 1, z);
             player.world.notify(x, y, z - 1);
             player.world.notify(x, y, z + 1);
        }
        super.a(packet);
    }
    @Override
    public void sendPacket(Packet packet){
        if(packet instanceof Packet51MapChunk){
             Packet51MapChunk dataPacket = (Packet51MapChunk) packet;
             this.breakPacketIntoChunks(dataPacket.a,dataPacket.b,dataPacket.c,dataPacket.d,dataPacket.e,dataPacket.f,dataPacket.rawData);
        }
        super.sendPacket(packet);
    }//sendPacket()

    private int replaceCoveredBlocks(Chunk chunk, int xPos, int yPos, int zPos, int xSize, int ySize, int zSize, int k1, byte abyte[]){
        int tracker = 0;
        byte[] newArray; //Create a temporary array that we're going to store our modified data in
        if(ySize == 128){ newArray = new byte[(xSize-xPos) * (ySize-yPos) * (zSize-zPos)]; }
        else { newArray = new byte[(ySize-yPos)]; }
        
        //Loop over all the blocks in this chunk
        for(int x=xPos; x<xSize || x==xPos; x++){
            for(int z=zPos; z<zSize || z==zPos; z++){
                tracker = 0;
                for(int y=yPos; y<ySize || y==yPos; y++){
                    int index = tracker++; //For partial chunk updates, we only loop over the y values in this function
                    if(ySize == 128){ index = (x << 11 | z << 7 | y); } //Use a different index if it's a full chunk update

                    if(index >= newArray.length) continue; //WorldEditing blocks outside the map can cause out of bounds errors

                    int type = chunk.getTypeId(x,y,z);
                    int worldX = chunk.x * 16 + x;
                    int worldZ = chunk.z * 16 + z;

                    newArray[index] = ((byte)(type & 0xff));
                    if(Arrays.binarySearch(this.hiddenBlocks, type) >= 0){
                        CHECKTYPE: //Check to see if there is air around the block
                        {
                            if(isBlockTransparent(chunk.world, worldX + 1, y, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX - 1, y, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, y + 1, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, y - 1, worldZ)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, y, worldZ + 1)) break CHECKTYPE;
                            if(isBlockTransparent(chunk.world, worldX, y, worldZ - 1)) break CHECKTYPE;
                            newArray[index] = ((byte)(1 & 0xff));
                        }
                    }
                }
            }
        }
        //Copy our temporary generated array data into the packet data field (abyte)
        System.arraycopy(newArray, 0, abyte, k1, newArray.length);
        return k1 + newArray.length;
    }

    //Allow ores to be "burried" in other things but still show up, eg torches and ladders
    //  since those blocks can be clicked through / seen through (Fixed a bug where chests under torches would be invisible
    public boolean isBlockTransparent(World world, int x, int y, int z){
        int blockType = world.getTypeId(x,y,z);
        return blockType == 0 || blockType == 50 || blockType == 65 || blockType == 66 || blockType == 75 || blockType == 76 || blockType == 77 || blockType == 55 || blockType == 69 || blockType == 39 || blockType == 40;
    }

    //This is done because the arrays are concatinated together inside the packet
    //  we can't directly access data for a given x,y,z because we don't know where in the packet
    //  it is without going through this, this is the getMultiChunkData() function
    private void breakPacketIntoChunks(int i, int j, int k, int l, int i1, int j1, byte abyte0[]){
        int k1 = i >> 4;
        int l1 = k >> 4;
        int i2 = i + l - 1 >> 4;
        int j2 = k + j1 - 1 >> 4;
        int k2 = 0;
        int l2 = j;
        int i3 = j + i1;

        if(l2 < 0) { l2 = 0; }
        if(i3 > 128) { i3 = 128; }

        for (int j3 = k1; j3 <= i2; ++j3) {
            int k3 = i - j3 * 16;
            int l3 = i + l - j3 * 16;

            if(k3 < 0) { k3 = 0; }
            if(l3 > 16) { l3 = 16; }

            for(int i4 = l1; i4 <= j2; ++i4) {
                int j4 = k - i4 * 16;
                int k4 = k + j1 - i4 * 16;

                if(j4 < 0) { j4 = 0; }
                if(k4 > 16) { k4 = 16; }

                Chunk chunk = player.world.getChunkAt(j3, i4);
                if(i1 == 128){ //It's a full chunk update
                    k2 = replaceCoveredBlocks(chunk, k3, l2, j4, l3, i3, k4, k2, abyte0);
                } else { //Partial chunk update
                    for (int subchunkx = k3; subchunkx < l3; ++subchunkx) {
                        for (int subchunkz = j4; subchunkz < k4; ++subchunkz) {
                            k2 = this.replaceCoveredBlocks(chunk, subchunkx, l2, subchunkz, subchunkx, i3, subchunkz, k2, abyte0);
                        }
                    }
                }
            }
        }
    }
}
