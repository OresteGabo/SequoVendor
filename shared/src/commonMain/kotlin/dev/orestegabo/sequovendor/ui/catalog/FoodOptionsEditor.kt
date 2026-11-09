package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import dev.orestegabo.sequo.design.SequoActionButton as Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.*
import sequovendor.shared.generated.resources.*

internal fun validMeasureAmount(raw: String?): Boolean = raw != null &&
    raw.matches(Regex("[0-9]{1,4}(\\.[0-9]{1,3})?")) && raw.toDoubleOrNull()?.let { it > 0 && it <= 1000 } == true

internal fun validSellingGroups(groups: List<CatalogFoodOptionGroup>): Boolean =
    groups.count { it.active && it.type == "MEASURE" } <= 1 && groups.all { group ->
        val variant = group.type in setOf("SIZE", "COLOR", "MEASURE")
        group.name.isNotBlank() && group.options.size in 1..50 && group.minSelections in 0..group.maxSelections &&
            group.maxSelections in 0..50 && group.minSelections <= group.options.size &&
            (!variant || group.minSelections == 1 && group.maxSelections == 1) &&
            (group.type != "MEASURE" || group.unit in listOf("KG", "G", "L", "ML", "M", "CM", "UNIT")) &&
            group.options.all { it.label.isNotBlank() && (group.type != "MEASURE" || validMeasureAmount(it.measureAmount)) }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FoodOptionsEditor(product: CatalogProduct, isSaving: Boolean, onSave: (List<CatalogFoodOptionGroup>) -> Unit) {
    var groups by remember(product.id, product.foodOptionGroups) { mutableStateOf(product.foodOptionGroups) }
    fun replace(index: Int, group: CatalogFoodOptionGroup) {
        groups = groups.mapIndexed { i, old -> if (i == index) group else old }
    }
    val clothing = appText(Res.string.selling_clothing_size)
    val shoes = appText(Res.string.selling_shoe_size)
    val weight = appText(Res.string.selling_weight)
    fun preset(name: String, type: String, labels: List<String>, unit: String? = null) {
        groups = groups + CatalogFoodOptionGroup("", name, required = true, minSelections = 1, maxSelections = 1,
            type = type, unit = unit, options = labels.map { CatalogFoodOption("", if (unit == null) it else "$it kg",
                measureAmount = it.takeIf { unit != null }) })
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(appText(Res.string.selling_options_title), style = MaterialTheme.typography.titleMedium)
        Text(appText(Res.string.selling_options_hint), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TextButton(enabled = !isSaving && groups.size < 20, onClick = { preset(clothing, "SIZE", listOf("XS", "S", "M", "L", "XL", "XXL")) }) { Text(clothing) }
            TextButton(enabled = !isSaving && groups.size < 20, onClick = { preset(shoes, "SIZE", (36..46).map { it.toString() }) }) { Text(shoes) }
            TextButton(enabled = !isSaving && groups.size < 20 && groups.none { it.active && it.type == "MEASURE" },
                onClick = { preset(weight, "MEASURE", listOf("0.5", "1", "2", "5"), "KG") }) { Text(weight) }
        }
        groups.forEachIndexed { groupIndex, group ->
            HorizontalDivider()
            OutlinedTextField(group.name, { replace(groupIndex, group.copy(name = it.take(120))) },
                label = { Text(appText(Res.string.catalog_editor_food_group_name)) }, enabled = !isSaving,
                singleLine = true, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("PREPARATION", "SIZE", "COLOR", "MEASURE", "OTHER").forEach { type ->
                    FilterChip(selected = group.type == type, enabled = !isSaving,
                        label = { Text(appText(when (type) {
                            "SIZE" -> Res.string.selling_type_size
                            "COLOR" -> Res.string.selling_type_color
                            "MEASURE" -> Res.string.selling_type_measure
                            "OTHER" -> Res.string.selling_type_other
                            else -> Res.string.selling_type_preparation
                        })) }, onClick = {
                            val variant = type in setOf("SIZE", "COLOR", "MEASURE")
                            replace(groupIndex, group.copy(type = type, unit = if (type == "MEASURE") group.unit ?: "KG" else null,
                                required = variant || group.required,
                                minSelections = if (variant) 1 else group.minSelections,
                                maxSelections = if (variant) 1 else group.maxSelections,
                                options = group.options.map { it.copy(measureAmount = if (type == "MEASURE") it.measureAmount else null,
                                    priceDeltaCfa = if (type == "MEASURE") 0 else it.priceDeltaCfa) }))
                        })
                }
            }
            if (group.type == "MEASURE") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("KG", "G", "L", "ML", "M", "CM", "UNIT").forEach { unit ->
                        FilterChip(selected = group.unit == unit, enabled = !isSaving,
                            onClick = { replace(groupIndex, group.copy(unit = unit)) }, label = { Text(unit) })
                    }
                }
                Text(appText(Res.string.selling_unit_price_hint).replace("{unit}", group.unit.orEmpty()),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            if (group.type !in setOf("SIZE", "COLOR", "MEASURE")) Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(group.minSelections > 0, { required -> replace(groupIndex,
                    group.copy(required = required, minSelections = if (required) 1 else 0)) }, enabled = !isSaving)
                Text(appText(Res.string.catalog_editor_food_required), Modifier.weight(1f))
                OutlinedTextField(group.maxSelections.toString(), { replace(groupIndex,
                    group.copy(maxSelections = it.filter(Char::isDigit).take(2).toIntOrNull() ?: 0)) },
                    label = { Text(appText(Res.string.catalog_editor_food_max_selections)) },
                    modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true)
            }
            group.options.forEachIndexed { optionIndex, option ->
                fun change(updated: CatalogFoodOption) { replace(groupIndex, group.copy(options = group.options.mapIndexed { i, old ->
                    if (i == optionIndex) updated else old
                })) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(option.label, { change(option.copy(label = it.take(140))) },
                        label = { Text(appText(Res.string.catalog_editor_food_option_label)) }, modifier = Modifier.weight(1f),
                        enabled = !isSaving, singleLine = true)
                    if (group.type == "MEASURE") OutlinedTextField(option.measureAmount.orEmpty(), { raw ->
                        val amount = raw.replace(',', '.').filter { it.isDigit() || it == '.' }.take(9)
                        change(option.copy(measureAmount = amount, priceDeltaCfa = 0))
                    }, label = { Text(appText(Res.string.selling_amount) + " (${group.unit.orEmpty()})") },
                        modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true,
                        isError = !option.measureAmount.isNullOrBlank() && !validMeasureAmount(option.measureAmount))
                    else OutlinedTextField(option.priceDeltaCfa.toString(),
                        { change(option.copy(priceDeltaCfa = it.filter(Char::isDigit).take(9).toIntOrNull() ?: 0)) },
                        label = { Text(appText(Res.string.catalog_editor_food_option_price)) },
                        modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(option.available, { change(option.copy(available = it)) }, enabled = !isSaving)
                    Text(appText(Res.string.catalog_editor_food_option_available), Modifier.weight(1f))
                    TextButton(enabled = !isSaving, onClick = { replace(groupIndex,
                        group.copy(options = group.options.filterIndexed { i, _ -> i != optionIndex })) }) { Text(appText(Res.string.catalog_options_remove)) }
                }
            }
            Row {
                TextButton(enabled = !isSaving && group.options.size < 50, onClick = { replace(groupIndex,
                    group.copy(options = group.options + CatalogFoodOption("", ""))) }) { Text(appText(Res.string.catalog_options_add_choice)) }
                TextButton(enabled = !isSaving, onClick = { groups = groups.filterIndexed { i, _ -> i != groupIndex } }) {
                    Text(appText(Res.string.catalog_options_remove_group))
                }
            }
        }
        TextButton(enabled = !isSaving && groups.size < 20, onClick = {
            groups = groups + CatalogFoodOptionGroup("", "", type = "OTHER", options = listOf(CatalogFoodOption("", "")))
        }) { Text(appText(Res.string.catalog_options_add_group)) }
        Button(onClick = { onSave(groups) }, enabled = !isSaving && groups != product.foodOptionGroups && validSellingGroups(groups)) {
            Text(appText(Res.string.catalog_options_save))
        }
    }
}
