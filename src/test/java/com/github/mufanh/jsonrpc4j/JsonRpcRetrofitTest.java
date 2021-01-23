package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class ChainHead {

        private List<Cid> cids;

        private List<Block> blocks;
    }

    @Data
    @JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Cid {

        @JsonProperty("/")
        private String hex;
    }

    @Data
    @JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Block {

        private String miner;

        private Ticket ticket;
    }

    @Data
    @JsonNaming(value = PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    static class Ticket {

        private String vRFProof;
    }
}