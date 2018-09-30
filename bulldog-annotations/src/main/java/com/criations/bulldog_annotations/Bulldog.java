package com.criations.bulldog_annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a target for Bulldog annotation processor
 * <pre><code>
 * {@literal @}
 * @Bulldog
 * interface Settings{
 *      // values
 *  }
 * </code></pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bulldog {
}
