import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.VarClientInt
import net.runelite.api.VarClientStr
import net.runelite.api.events.GameTick
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@CompileStatic
@InheritConstructors
class Demo extends ScriptedPlugin {
	void onTick(GameTick e)
	{
		Widget widget = _client.getWidget(162, 45);

		if (widget != null) {
			_client.setVar(VarClientInt.INPUT_TYPE, 7);
			_client.setVar(VarClientStr.INPUT_TEXT, String.valueOf(4));
			_clientThread.invoke(() -> {this._client.runScript(681, null)});
			_clientThread.invoke(() -> {this._client.runScript(299, 0, 0)});
		}
	}

	void startup() {
		_eventBus.subscribe(GameTick.class, this, this.&onTick as Consumer<GameTick>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}
