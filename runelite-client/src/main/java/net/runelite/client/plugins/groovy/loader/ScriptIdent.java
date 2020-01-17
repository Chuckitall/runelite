package net.runelite.client.plugins.groovy.loader;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Slf4j
public class ScriptIdent
{
	String scriptPackage;
	String mainFile;
}
