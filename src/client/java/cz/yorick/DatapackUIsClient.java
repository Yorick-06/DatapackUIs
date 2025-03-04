package cz.yorick;

import com.google.gson.*;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class DatapackUIsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
	}

	public static void copyItemData(ItemStack stack) {
		GLFW.glfwSetClipboardString(MinecraftClient.getInstance().getWindow().getHandle(), getItemData(stack));
		sendMessage(Text.literal("Copied item data to clipboard"));
	}

	private static String getItemData(ItemStack stack) {
		JsonObject json = new JsonObject();
		appendName(stack, json);
		appendLore(stack, json);
		json.add("display", getDisplay(stack));

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(json);
	}

	private static void appendName(ItemStack stack, JsonObject json) {
		Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
		if(name == null) {
			return;
		}

		JsonElement jsonName = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, name).mapOrElse(element -> element, error -> new JsonPrimitive("Error while parsing the name: " + error.message()));
		json.add("name", jsonName);
	}

	private static void appendLore(ItemStack stack, JsonObject json) {
		LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
		if(loreComponent == null) {
			return;
		}

		List<Text> lore = loreComponent.lines();
		if(lore.size() == 0) {
			return;
		}

		JsonArray serializedLore = new JsonArray();
		lore.stream().map(text -> TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text)
				.mapOrElse(
						element -> element,
						error -> new JsonPrimitive("Error while parsing this lore line: " + error.message())
				)
		).forEach(serializedLore::add);

		json.add("lore", serializedLore);
	}

	private static JsonElement getDisplay(ItemStack stack) {
		ItemStack serializedStack = stack.copy();
		//name and lore is getting serialized outside of display
		serializedStack.remove(DataComponentTypes.CUSTOM_NAME);
		serializedStack.remove(DataComponentTypes.LORE);

		//if there are no component changes, return it as an id
		if(serializedStack.getComponentChanges().size() == 0) {
			return new JsonPrimitive(Registries.ITEM.getId(stack.getItem()).toString());
		}

		//if the item is a player head and the only valid changed component is the profile, try to get the player head texture
		if(stack.getItem() instanceof PlayerHeadItem && serializedStack.getComponentChanges().size() == 1 && serializedStack.hasChangedComponent(DataComponentTypes.PROFILE)) {
			String texture = getHeadTexture(stack);
			if(texture != null) {
				return  new JsonPrimitive(texture);
			}
		}

		//for there are more changes just serialize the stack
		return ItemStack.VALIDATED_CODEC.encodeStart(RegistryOps.of(JsonOps.INSTANCE, MinecraftClient.getInstance().player.getRegistryManager()), serializedStack).mapOrElse(
				element -> element,
				//if the process failed, return the items id
				error -> {
					sendMessage(Text.literal("An error occurred while serializing the stack, returning only id: " + error).formatted(Formatting.RED));
					return new JsonPrimitive(Registries.ITEM.getId(serializedStack.getItem()).toString());
				}
		);
	}

	private static String getHeadTexture(ItemStack stack) {
		ProfileComponent profile = stack.get(DataComponentTypes.PROFILE);
		if(profile == null) {
			return null;
		}

		PropertyMap propertyMap = profile.properties();
		if(!propertyMap.containsKey("textures")) {
			return null;
		}

		List<String> textures = propertyMap.get("textures").stream().map(Property::value).toList();
		if(textures.size() > 1) {
			sendMessage(Text.literal(" Multiple textures (" + textures.size() + ") found on this head - only copying first!").formatted(Formatting.RED));
		}

		return textures.getFirst();
	}

	private static void sendMessage(Text text) {
		MinecraftClient.getInstance().player.sendMessage((Text.literal("[" + DatapackUIs.MOD_ID + "] ").formatted(Formatting.GREEN).append(text)), false);
	}
}