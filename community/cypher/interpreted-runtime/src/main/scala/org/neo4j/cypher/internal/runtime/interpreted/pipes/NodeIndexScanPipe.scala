/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.runtime.interpreted.pipes

import org.neo4j.cypher.internal.runtime.QueryContext
import org.neo4j.cypher.internal.runtime.interpreted.ExecutionContext
import org.neo4j.cypher.internal.v3_5.logical.plans.IndexedProperty
import org.neo4j.internal.kernel.api.IndexReference
import org.opencypher.v9_0.expressions.LabelToken
import org.opencypher.v9_0.util.attribution.Id

case class NodeIndexScanPipe(ident: String,
                             label: LabelToken,
                             property: IndexedProperty)
                            (val id: Id = Id.INVALID_ID) extends Pipe with IndexPipeWithValues {

  override val propertyIndicesWithValues: Array[Int] = if (property.shouldGetValue) Array(0) else Array.empty
  override val propertyNamesWithValues: Array[String] = if (property.shouldGetValue) Array(ident + "." + property.propertyKeyToken.name) else Array.empty


  private var reference: IndexReference = IndexReference.NO_INDEX

  private def reference(context: QueryContext): IndexReference = {
    if (reference == IndexReference.NO_INDEX) {
      reference = context.indexReference(label.nameId.id, property.propertyKeyToken.nameId.id)
    }
    reference
  }
  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext] = {
    val baseContext = state.createOrGetInitialContext(executionContextFactory)
    val results = state.query.indexScan(reference(state.query), propertyIndicesWithValues)
    createResultsFromTupleIterator(baseContext, results)
  }
}
