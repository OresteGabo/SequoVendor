package dev.orestegabo.sequovendor.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequovendor.feature.settings.appText
import dev.orestegabo.sequovendor.model.CatalogFoodOption
import dev.orestegabo.sequovendor.model.CatalogFoodOptionGroup
import dev.orestegabo.sequovendor.model.CatalogProduct
import sequovendor.shared.generated.resources.*

@Composable
internal fun FoodOptionsEditor(
    product: CatalogProduct,
    isSaving: Boolean,
    onSave: (List<CatalogFoodOptionGroup>) -> Unit,
) {
    var groups by remember(product.id, product.foodOptionGroups) { mutableStateOf(product.foodOptionGroups) }

    fun replace(index: Int, group: CatalogFoodOptionGroup) {
        groups = groups.mapIndexed { i, old -> if (i == index) group else old }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(appText(Res.string.catalog_editor_food_title))
        Text(appText(Res.string.catalog_editor_food_local_note))
        groups.forEachIndexed { groupIndex, group ->
            OutlinedTextField(
                group.name, { replace(groupIndex, group.copy(name = it.take(120))) },
                label = { Text(appText(Res.string.catalog_editor_food_group_name)) },
                enabled = !isSaving, singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(group.minSelections > 0, { required ->
                    replace(groupIndex, group.copy(required = required, minSelections = if (required) 1 else 0))
                }, enabled = !isSaving)
                Text(appText(Res.string.catalog_editor_food_required), Modifier.weight(1f))
                OutlinedTextField(
                    group.maxSelections.toString(), {
                        replace(groupIndex, group.copy(maxSelections = it.filter(Char::isDigit).take(2).toIntOrNull() ?: 0))
                    },
                    label = { Text(appText(Res.string.catalog_editor_food_max_selections)) },
                    modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true,
                )
            }
            group.options.forEachIndexed { optionIndex, option ->
                fun change(updated: CatalogFoodOption) {
                    replace(groupIndex, group.copy(options = group.options.mapIndexed { i, old ->
                        if (i == optionIndex) updated else old
                    }))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        option.label, { change(option.copy(label = it.take(140))) },
                        label = { Text(appText(Res.string.catalog_editor_food_option_label)) },
                        modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true,
                    )
                    OutlinedTextField(
                        option.priceDeltaCfa.toString(),
                        { change(option.copy(priceDeltaCfa = it.filter(Char::isDigit).take(9).toIntOrNull() ?: 0)) },
                        label = { Text(appText(Res.string.catalog_editor_food_option_price)) },
                        modifier = Modifier.weight(1f), enabled = !isSaving, singleLine = true,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(option.available, { change(option.copy(available = it)) }, enabled = !isSaving)
                    Text(appText(Res.string.catalog_editor_food_option_available), Modifier.weight(1f))
                    TextButton(enabled = !isSaving, onClick = {
                        replace(groupIndex, group.copy(options = group.options.filterIndexed { i, _ -> i != optionIndex }))
                    }) { Text(appText(Res.string.catalog_options_remove)) }
                }
            }
            Row {
                TextButton(enabled = !isSaving && group.options.size < 50, onClick = {
                    replace(groupIndex, group.copy(options = group.options + CatalogFoodOption("", "")))
                }) { Text(appText(Res.string.catalog_options_add_choice)) }
                TextButton(enabled = !isSaving, onClick = {
                    groups = groups.filterIndexed { i, _ -> i != groupIndex }
                }) { Text(appText(Res.string.catalog_options_remove_group)) }
            }
        }
        TextButton(enabled = !isSaving && groups.size < 20, onClick = {
            groups = groups + CatalogFoodOptionGroup("", "", options = listOf(CatalogFoodOption("", "")))
        }) { Text(appText(Res.string.catalog_options_add_group)) }
        Button(
            onClick = { onSave(groups) },
            enabled = !isSaving && groups != product.foodOptionGroups && groups.all { group ->
                group.name.isNotBlank() && group.minSelections <= group.maxSelections &&
                    group.maxSelections in 0..50 && group.minSelections <= group.options.size && group.options.isNotEmpty() &&
                    group.options.all { it.label.isNotBlank() }
            },
        ) { Text(appText(Res.string.catalog_options_save)) }
    }
}
