package io.seda.inventory.configuration

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import io.seda.inventory.data.ItemReadConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.dialect.R2dbcDialect


@Configuration
class DatabaseConfiguration: AbstractR2dbcConfiguration() {
    @Value("\${database.database}") lateinit var dbName: String;
    @Value("\${database.username}") lateinit var username: String;
    @Value("\${database.password}") lateinit var password: String;
    @Value("\${database.hostname}") lateinit var hostname: String;
    @Value("\${database.schema}") lateinit var schema: String;
    @Value("\${database.port}") lateinit var port: String;

    @Bean
    override fun connectionFactory(): ConnectionFactory {// literal definition of no-brain api.
        val config = PostgresqlConnectionConfiguration.builder()
            .host(hostname)
            .port(port.toInt())
            .username(username)
            .password(password)
            .database(dbName)
            .schema(schema).build()
        return PostgresqlConnectionFactory(config);
    }

    override fun getDialect(connectionFactory: ConnectionFactory): R2dbcDialect {
        val dialect = super.getDialect(connectionFactory) as PostgresDialect
        return dialect
    }

    override fun getCustomConverters(): MutableList<Converter<*, *>> {
        val converters: MutableList<Converter<*, *>> = ArrayList()
        converters.add(ItemReadConverter)
        return converters
    }
}