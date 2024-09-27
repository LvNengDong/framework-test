/**
 * @Author lnd
 * @Description
 *
 *  拼凑 SQL 语句是一件烦琐且易出错的过程，为了将开发人员从这项枯燥无趣的工作中 解脱出来，MyBatis 实现动态 SQL 语句的功能，提供了多种动态SQL语句对应的节点。
 *  例如<where> 节点、<if> 节点、<foreach> 节点等 。通过这些节点的组合使用， 开发人 员可以写出几乎满足所有需求的动态 SQL 语句。
 *
 *  MyBatis 中的 scripting 模块，会根据用户传入的实参，解析映射文件中定义的动态 SQL 节点，并形成数据库可执行的 SQL 语句。之后会处理 SQL 语句中的占位符，绑定用户传入的实参。
 *
 *  总结来说，scripting 模块，最大的作用，就是实现了 MyBatis 的动态 SQL 语句的功能。关于这个功能，对应文档为 《MyBatis 文档 —— 动态 SQL》 。
 *      https://mybatis.org/mybatis-3/zh_CN/dynamic-sql.html
 *
 * @Date 2024/9/27 14:07
 */
package cn.lnd.ibatis.scripting;