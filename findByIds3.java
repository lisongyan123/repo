@Override
public List<ProductImpl> findByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        log.debug("Empty or null ids collection provided to findByIds.");
        return Collections.emptyList();
    }

    Session session = this.getSessionFactory().getCurrentSession();

    // 使用 Hibernate 的 byMultipleIds 进行批量加载
    @SuppressWarnings("unchecked")
    List<ProductImpl> instances = session.byMultipleIds(ProductImpl.class)
                                         .multiLoad(ids);

    // 过滤掉 null 值并进行反代理处理
    List<ProductImpl> result = new ArrayList<>();
    for (ProductImpl instance : instances) {
        if (instance != null) {
            // 反代理，确保返回的是真实对象而非 Hibernate 代理
            ProductImpl deproxied = HibernateDeproxyHelper.deproxy(instance);
            result.add(deproxied);
        } else {
            if (log.isDebugEnabled()) {
                // 注意：无法知道哪个具体 ID 缺失，除非传入顺序一致且可映射
                log.debug("One product not found in batch load (null returned).");
            }
        }
    }

    return result;
}
