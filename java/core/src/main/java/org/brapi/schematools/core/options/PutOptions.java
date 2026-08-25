package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of PUT (update) Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
public class PutOptions extends AbstractUpdateSubOptions {
	@Setter(AccessLevel.PRIVATE)
	private Map<String, Map<String, Boolean>> embeddedResponsePropertiesFor = new HashMap<>();

	@Override
	public void override(AbstractUpdateSubOptions overrideOptions) {
		super.override(overrideOptions);

		if (overrideOptions instanceof PutOptions putOptions && putOptions.embeddedResponsePropertiesFor != null) {
			putOptions.embeddedResponsePropertiesFor.forEach((key, value) -> {
				if (value == null) {
					embeddedResponsePropertiesFor.remove(key);
				} else if (embeddedResponsePropertiesFor.containsKey(key)) {
					value.forEach((innerKey, innerValue) -> {
						if (innerValue == null) embeddedResponsePropertiesFor.get(key).remove(innerKey);
						else embeddedResponsePropertiesFor.get(key).put(innerKey, innerValue);
					});
					if (embeddedResponsePropertiesFor.get(key).isEmpty()) embeddedResponsePropertiesFor.remove(key);
				} else {
					embeddedResponsePropertiesFor.put(key, new HashMap<>(value));
				}
			});
		}
	}

	/**
	 * Determines if a property should be embedded only in the PUT /{id} response
	 * for the supplied primary model.
	 *
	 * @param type the primary model
	 * @param property the property to consider
	 * @return {@code true} if the property is embedded in the PUT response
	 */
	@JsonIgnore
	public boolean isEmbeddingResponsePropertyFor(@NonNull BrAPIObjectType type, @NonNull BrAPIObjectProperty property) {
		Map<String, Boolean> map = embeddedResponsePropertiesFor.get(type.getName());
		if (map != null) {
			Boolean value = map.get(property.getName());
			return value != null && value;
		}
		return false;
	}
}
