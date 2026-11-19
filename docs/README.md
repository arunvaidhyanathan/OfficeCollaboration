# Documentation

Documentation for the Office Collaboration POC, generated against commit `dd04d4a`.

| Folder | What it is | Tool |
|---|---|---|
| `s1000d/` | Technical publication in ASD S1000D Issue 5.0 XML (publication module + data modules) | hand-authored, schema-validated |
| `diagrams/` | Interactive system architecture diagram (`system-architecture.html`) and its source JSON | archify |
| `graphify/` | Knowledge graph of the codebase: `GRAPH_REPORT.md`, interactive `graph.html`, `graph.json` | graphify |
| `codegraph/` | Symbol index report: status, circular imports, change impact, file structure | codegraph |
| `architecture/` | Original architecture ADR (HTML) | - |

## S1000D publication

Start at the publication module `s1000d/PMC-OFFCOLLAB-A0001-00_001-00_EN-US.XML`. It references:

| Data module code | Type | Title |
|---|---|---|
| `OFFCOLLAB-A-00-00-00-00A-040A-D` | descriptive | Office Collaboration Platform - General description |
| `OFFCOLLAB-A-01-00-00-00A-040A-D` | descriptive | Document Core service - Description of function |
| `OFFCOLLAB-A-02-00-00-00A-040A-D` | descriptive | Data storage - Description |
| `OFFCOLLAB-A-00-00-00-00A-100A-D` | procedural | Office Collaboration Platform - Start the local stack |

SNS used: system code `00` whole platform, `01` Document Core, `02` data storage. All modules are
`qualityAssurance/unverified` (first issue, not technically reviewed).

To validate, download the S1000D 5.0 flat schemas (`descript.xsd`, `proced.xsd`, `pm.xsd`, plus
`xlink.xsd`, `rdf.xsd`, `dc.xsd`) from `http://www.s1000d.org/S1000D_5-0/xml_schema_flat/` into one
folder and run `xmllint --noout --schema <folder>/descript.xsd s1000d/DMC-*040A*.XML` (and likewise
`proced.xsd` for `100A`, `pm.xsd` for the PMC). BREX rules are not checked.

## Regenerating

```bash
graphify update .            # writes graphify-out/; copy GRAPH_REPORT.md, graph.html, graph.json to docs/graphify/
codegraph index .            # then re-run the report commands at the top of docs/codegraph/CODEGRAPH_REPORT.md
```
