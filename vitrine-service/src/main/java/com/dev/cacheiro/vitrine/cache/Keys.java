package com.dev.cacheiro.vitrine.cache;


public final class Keys {

    private static final String PREFIXO = "vitrine:";

    public static String produto(long id) { return PREFIXO + "produto:" + id; }

    public static String lockProduto(long id) { return PREFIXO + "lock:produto:" + id; }

    public static final String PRODUTOS_ALL = PREFIXO + "produtos:all";

    public static String rateLimit(String ip) { return PREFIXO + "ratelimit:" + ip; }

    private Keys() {}
}
