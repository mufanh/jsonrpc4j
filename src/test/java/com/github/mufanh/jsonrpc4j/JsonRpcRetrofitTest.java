package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.mufanh.jsonrpc4j.annotation.JsonRpcMethod;
import com.github.mufanh.jsonrpc4j.annotation.JsonRpcService;
import lombok.Data;
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
        System.out.println(JsonUtils.toJSONString(response.getResult()));
    }

    @JsonRpcService
    interface FileCoinRpc {

        @JsonRpcMethod("Filecoin.ChainHead")
        Call<ChainHead> chainHead();
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class ChainHead {

        private List<Cid> cids;

        private List<Block> blocks;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Cid {

        @JsonProperty("/")
        private String hex;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Block {

        private String miner;

        private Ticket ticket;

        private ElectionProof electionProof;

        private List<BeaconEntry> beaconEntries;

        private List<WinPoStProof> winPoStProof;

        private List<Cid> parents;

        private String parentWeight;

        private long height;

        private Cid parentStateRoot;

        private Cid parentMessageReceipts;

        private Cid messages;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Ticket {

        private String vRFProof;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class ElectionProof {

        private int winCount;

        private String vRFProof;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class BeaconEntry {

        private long round;

        private String data;
    }

    @Data
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class WinPoStProof {

        private long poStProof;

        private String ProofBytes;
    }
}