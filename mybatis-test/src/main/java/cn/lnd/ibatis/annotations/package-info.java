/**
 * @Author lnd
 * @Description

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    随着 Java 注解的慢慢流行，MyBatis 提供了注解的方式，使得我们方便的在 Mapper 接口上编写简单的数据库 SQL 操作代码，
    而无需像之前一样，必须编写 SQL 在 XML 格式的 Mapper 文件中。

    虽然说，实际场景下，大家还是喜欢在 XML 格式的 Mapper 文件中编写响应的 SQL 操作。


mybatis提供的注解有很多，笔者进行了分类：
    增删改查：
        @Insert
        @Update
        @Delete
        @Select
        @MapKey
        @Options
        @SelelctKey
        @Param
        @InsertProvider
        @UpdateProvider
        @DeleteProvider
        @SelectProvider
    结果集映射：
        @Results
        @Result
        @ResultMap
        @ResultType
        @ConstructorArgs
        @Arg
        @One
        @Many
        @TypeDiscriminator
        @Case
    缓存：
        @CacheNamespace
        @Property
        @CacheNamespaceRef
        @Flush
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

 * @Date 2024/9/21 01:22
 */
package cn.lnd.ibatis.annotations;