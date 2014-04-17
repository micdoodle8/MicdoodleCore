package micdoodle8.mods.miccore;

import java.io.IOException;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class MicdoodleAccessTransformer extends AccessTransformer
{
	public MicdoodleAccessTransformer() throws IOException
	{
		super("micdoodlecore_at.cfg");
	}
}