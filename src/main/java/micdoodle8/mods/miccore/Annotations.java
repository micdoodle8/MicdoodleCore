package micdoodle8.mods.miccore;

import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Annotations
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface RuntimeInterface
	{
		String clazz();

		String modID();

		String[] altClasses() default {};
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface AltForVersion
	{
		String version();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NetworkedField
	{
		Side targetSide();
	}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface VersionSpecific
    {
        String version();
    }
}
