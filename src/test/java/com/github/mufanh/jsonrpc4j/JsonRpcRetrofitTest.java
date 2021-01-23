package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mufanh.jsonrpc4j.annotation.JsonRpcMethod;
import com.github.mufanh.jsonrpc4j.annotation.JsonRpcService;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author xinquan.huangxq
 */
public class JsonRpcRetrofitTest {

    @Test
    public void jsonRpcTest() throws IOException {
        JsonRpcRetrofit jsonRpcRetrofit = new JsonRpcRetrofit.Builder()
                .httpUrl("http://192.168.77.252:1234/rpc/v0")
                .build();
        FileCoinRpc fileCoinRpc = jsonRpcRetrofit.create(FileCoinRpc.class);
        Response<ChainHead> response = fileCoinRpc.chainHead().execute();
    }

    @JsonRpcService
    interface FileCoinRpc {

        @JsonRpcMethod("Filecoin.ChainHead")
        Call<ChainHead> chainHead();
    }

    static class ChainHead {

        private List<Cid> Cids;

        private List<Block> Blocks;
    }

    static class Cid {

        @JsonProperty("/")
        private String Hex;
    }

    static class Block {

        private String Miner;

        private Ticket Ticket;
    }

    static class Ticket {

        private String VRFProof;
    }
}