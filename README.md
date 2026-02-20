public Map<Long, ExchangeCountryCode> getExchangeCountriesNative(Collection<Long> productNumbers) {
    if (productNumbers.isEmpty()) return Collections.emptyMap();

    String sql = """
        SELECT 
            p.PROD_NUM,
            CASE 
                WHEN p.MDUL_SYS_CDE = 'SEC' THEN s.EXCH_COUNTRY_CDE  -- 假设 stock_instm 表中字段叫 EXCH_COUNTRY_CDE
                WHEN p.MDUL_SYS_CDE = 'TG' THEN 'HK'
                ELSE NULL
            END AS EXCHANGE_COUNTRY_CODE
        FROM PROD p
        LEFT JOIN stock_instm s ON p.PROD_NUM = s.prod_num
        WHERE p.PROD_NUM IN (:prodNums)
        """;

    Query nativeQuery = entityManager.createNativeQuery(sql);
    nativeQuery.setParameter("prodNums", productNumbers);

    @SuppressWarnings("unchecked")
    List<Object[]> rows = nativeQuery.getResultList();

    Map<Long, ExchangeCountryCode> result = new HashMap<>();
    for (Object[] row : rows) {
        Long prodNum = ((Number) row[0]).longValue();
        String codeStr = (String) row[1];
        ExchangeCountryCode code = codeStr != null ? ExchangeCountryCode.valueOf(codeStr) : null;
        result.put(prodNum, code);
    }
    return result;
}
