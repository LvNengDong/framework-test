/**
 * @Author lnd
 * @Description
 *
 * 在调用 SqlSession 相应方法执行数据库操作时，需要指定映射文件中定义的 SQL 节点，如果出现拼写错误，我们只能在运行时才能发现相应的异常。
 * 为了尽早发现这种错误，MyBatis 通过 Binding 模块，将用户自定义的 Mapper 接口与映射配置文件关联起来，系统可以通过调用自定义 Mapper
 * 接口中的方法执行相应的 SQL 语句完成数据库操作，从而避免上述问题。
 *
 * 值得读者注意的是，开发人员无须编写自定义 Mapper 接口的实现，MyBatis 会自动为其创建动态代理对象。在有些场景中，自定义 Mapper 接口可以
 * 完全代替映射配置文件，但有的映射规则和 SQL 语句的定义还是写在映射配置文件中比较方便，例如动态 SQL 语句的定义。
 *
 *
 * @Date 2024/9/20 23:39
 */
package cn.lnd.ibatis.binding;