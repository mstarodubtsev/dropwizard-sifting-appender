package com.github.mstarodubtsev.dropwizard;

import java.util.TimeZone;

import javax.validation.constraints.NotNull;

import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.validation.ValidationMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.sift.AppenderFactory;

/**
 * An {@link AppenderFactory} implementation which provides an appender that splits events to separate log files depending on MDC context.
 * <b>Configuration Parameters:</b>
 * <table summary="">
 *     <tr>
 *         <th class="head">Name</th>
 *         <th class="head">Default</th>
 *         <th class="head">Description</th>
 *     </tr>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code sift}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to processing by appender.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeZone}</td>
 *         <td>{@code UTC}</td>
 *         <td>The time zone to which event timestamps will be converted.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code discriminatorKey}</td>
 *         <td>{@code logfileName}</td>
 *         <td>Discriminator key for sift events</td>
 *     </tr>
 *     <tr>
 *         <td>{@code discriminatorDefaultValue}</td>
 *         <td>{@code logfileName}</td>
 *         <td>Discriminator default value</td>
 *     </tr>
 *     <tr>
 *         <td>{@code messagePattern}</td>
 *         <td>the default format</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("sift")
public class SiftingAppenderFactory extends AbstractAppenderFactory {
    @NotNull
    private String discriminatorKey;

    @NotNull
    private String discriminatorDefaultValue;

    @NotNull
    private String messagePattern;

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    public String getDiscriminatorKey() {
        return discriminatorKey;
    }

    public void setDiscriminatorKey(String discriminatorKey) {
        this.discriminatorKey = discriminatorKey;
    }

    public String getDiscriminatorDefaultValue() {
        return discriminatorDefaultValue;
    }

    public void setDiscriminatorDefaultValue(String discriminatorDefaultValue) {
        this.discriminatorDefaultValue = discriminatorDefaultValue;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public void setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonIgnore
    @ValidationMethod(message = "some message")
    public boolean isValidArchiveConfiguration() {
        return true;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final SiftingAppender siftingAppender = new SiftingAppender();
        siftingAppender.setName("sift-appender");
        siftingAppender.setContext(context);
        addThresholdFilter(siftingAppender, threshold);
        
        MDCBasedDiscriminator mdcBasedDiscriminator = new MDCBasedDiscriminator();
        mdcBasedDiscriminator.setKey(discriminatorKey);
        mdcBasedDiscriminator.setDefaultValue(discriminatorDefaultValue);
        mdcBasedDiscriminator.start();
        siftingAppender.setDiscriminator(mdcBasedDiscriminator);
        
        siftingAppender.setAppenderFactory(new AppenderFactory<ILoggingEvent>() {

            @Override
            public Appender<ILoggingEvent> buildAppender(Context context, String discriminatingValue) throws JoranException {
                FileAppender<ILoggingEvent> appender = new FileAppender<>();
                appender.setAppend(true);
                
                appender.setName("FILE-" + discriminatingValue);
                appender.setContext(context);
                appender.setFile("log-" + discriminatingValue + ".log");
                appender.setPrudent(false);
                //appender.setLayout(layout == null ? buildLayout(context, timeZone) : layout);

                PatternLayoutEncoder pl = new PatternLayoutEncoder();
                pl.setContext(context);
                //pl.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}: %msg%n");
                pl.setPattern(messagePattern);
                pl.start();
                appender.setEncoder(pl);

                appender.start();
                return appender;
            }

        });
        siftingAppender.start();

        return wrapAsync(siftingAppender);
    }
}