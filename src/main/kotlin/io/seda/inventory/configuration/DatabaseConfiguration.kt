package io.seda.inventory.configuration

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.BuiltinDynamicCodecs
import io.r2dbc.postgresql.codec.VectorFloatCodec
import io.r2dbc.spi.ConnectionFactory
import io.seda.inventory.data.ItemReadConverter
import io.seda.inventory.data.VectorFloatConverter
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
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest


@Configuration
class DatabaseConfiguration: AbstractR2dbcConfiguration() {
    @Value("\${database.database}") lateinit var dbName: String;
    @Value("\${database.username}") lateinit var username: String;
    @Value("\${DATABASE_PASSWORD_PARAM_NAME}") lateinit var password: String;
    @Value("\${database.hostname}") lateinit var hostname: String;
    @Value("\${database.schema}") lateinit var schema: String;
    @Value("\${database.port}") lateinit var port: String;

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        var client: SsmAsyncClient = SsmAsyncClient.create();
        val password = client.getParameter(GetParameterRequest.builder().withDecryption(true).name(password).build()).get().parameter().value();

        val config = PostgresqlConnectionConfiguration.builder()
            .host(hostname)
            .port(port.toInt())
            .username(username)
            .password(password)
            .database(dbName)
            .codecRegistrar(BuiltinDynamicCodecs())
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
        converters.add(VectorFloatConverter)
        return converters
    }
}