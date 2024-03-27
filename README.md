# VéloCityDB

    ██╗   ██╗███████╗██╗      ██████╗  ██████╗██╗████████╗██╗   ██╗██████╗ ██████╗ 
    ██║   ██║██╔════╝██║     ██╔═══██╗██╔════╝██║╚══██╔══╝╚██╗ ██╔╝██╔══██╗██╔══██╗
    ██║   ██║█████╗  ██║     ██║   ██║██║     ██║   ██║    ╚████╔╝ ██║  ██║██████╔╝
    ╚██╗ ██╔╝██╔══╝  ██║     ██║   ██║██║     ██║   ██║     ╚██╔╝  ██║  ██║██╔══██╗
     ╚████╔╝ ███████╗███████╗╚██████╔╝╚██████╗██║   ██║      ██║   ██████╔╝██████╔╝
      ╚═══╝  ╚══════╝╚══════╝ ╚═════╝  ╚═════╝╚═╝   ╚═╝      ╚═╝   ╚═════╝ ╚═════╝ 

This project creates an SQLite database gathering bicycle traffic measurements from several cities.

The database itself is not released as an artifact because of its large size. Instead, you can use this project to
create that database locally.

Cities gather their bicycle traffic differently. For each measurement, VéloCityDB standardises that variety of data into
the following dimensions:

- City name
- Measurement location
    - These are usually traffic counting terminals spread across the city
- Hourly traffic count
- Measurement timestamp

> [!NOTE]<br>
> VéloCityDB is a great building block for use cases such as data analysis and dashboards.
>
> For inspiration, you can have a look at query and visualisation
> examples [here](./velo-city-db/tree/main/examples-queries/ExampleQueries.md).

Cities currently supported are, in alphabetic order:

| Country        | City            | Data Source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | Data Format | Data Licence                                                                                |
|----------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|---------------------------------------------------------------------------------------------|
| France         | Bordeaux        | [Capteur de trafic vélo - historique horaire](https://opendata.bordeaux-metropole.fr/explore/dataset/pc_captv_p_histo_heure/information/?disjunctive.gid&disjunctive.ident)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | CSV         | [Licence Ouverte / Open Licence](https://www.etalab.gouv.fr/licence-ouverte-open-licence)   |
| France         | Nantes          | [Comptages vélo de Nantes Métropole](https://data.nantesmetropole.fr/explore/dataset/244400404_comptages-velo-nantes-metropole/export/?disjunctive.boucle_num&disjunctive.libelle&disjunctive.jour_de_la_semaine&disjunctive.boucle_libelle&sort=jour&dataChart=eyJxdWVyaWVzIjpbeyJjaGFydHMiOlt7InR5cGUiOiJsaW5lIiwiZnVuYyI6IlNVTSIsInlBeGlzIjoidG90YWwiLCJzY2llbnRpZmljRGlzcGxheSI6dHJ1ZSwiY29sb3IiOiIjMDg3RkEzIn1dLCJ4QXhpcyI6ImpvdXIiLCJtYXhwb2ludHMiOiIiLCJ0aW1lc2NhbGUiOiJkYXkiLCJzb3J0IjoiIiwiY29uZmlnIjp7ImRhdGFzZXQiOiIyNDQ0MDA0MDRfY29tcHRhZ2VzLXZlbG8tbmFudGVzLW1ldHJvcG9sZSIsIm9wdGlvbnMiOnsiZGlzanVuY3RpdmUuYm91Y2xlX251bSI6dHJ1ZSwiZGlzanVuY3RpdmUubGliZWxsZSI6dHJ1ZSwiZGlzanVuY3RpdmUuam91cl9kZV9sYV9zZW1haW5lIjp0cnVlLCJkaXNqdW5jdGl2ZS5ib3VjbGVfbGliZWxsZSI6dHJ1ZSwic29ydCI6ImpvdXIifX19XSwiZGlzcGxheUxlZ2VuZCI6dHJ1ZSwiYWxpZ25Nb250aCI6dHJ1ZSwidGltZXNjYWxlIjoiIn0%3D) | CSV         | [Open Data Commons Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl) |
| France         | Paris           | [Comptage vélo - Données compteurs](https://opendata.paris.fr/explore/dataset/comptage-velo-donnees-compteurs/information/?disjunctive.id_compteur&disjunctive.nom_compteur&disjunctive.id&disjunctive.name&dataChart=eyJxdWVyaWVzIjpbeyJjaGFydHMiOlt7InR5cGUiOiJjb2x1bW4iLCJmdW5jIjoiQVZHIiwieUF4aXMiOiJzdW1fY291bnRzIiwic2NpZW50aWZpY0Rpc3BsYXkiOnRydWUsImNvbG9yIjoiI0ZBOEM0NCJ9XSwieEF4aXMiOiJkYXRlIiwibWF4cG9pbnRzIjoiIiwidGltZXNjYWxlIjoibW9udGgiLCJzb3J0IjoiIiwiY29uZmlnIjp7ImRhdGFzZXQiOiJjb21wdGFnZS12ZWxvLWRvbm5lZXMtY29tcHRldXJzIiwib3B0aW9ucyI6eyJkaXNqdW5jdGl2ZS5pZF9jb21wdGV1ciI6dHJ1ZSwiZGlzanVuY3RpdmUubm9tX2NvbXB0ZXVyIjp0cnVlLCJkaXNqdW5jdGl2ZS5pZCI6dHJ1ZSwiZGlzanVuY3RpdmUubmFtZSI6dHJ1ZX19fV0sImRpc3BsYXlMZWdlbmQiOnRydWUsImFsaWduTW9udGgiOnRydWUsInRpbWVzY2FsZSI6IiJ9)                                                                              | CSV         | [Open Data Commons Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl) |
| France         | Rennes          | [Comptages vélo](https://data.rennesmetropole.fr/explore/dataset/eco-counter-data/information/)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | CSV         | [Open Data Commons Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl) |
| France         | Strasbourg      | [strasbourgvelo.fr](https://strasbourgvelo.fr/compteurs.csv) (processed by fetching data from [SIRAC - flux trafic en temps réel](https://data.strasbourg.eu/explore/dataset/sirac_flux_trafic/information))                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | CSV         | [Open Data Commons Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl) |
| United Kingdom | Camden - London | [Camden Cycle Counters Phase 2](https://opendata.camden.gov.uk/Transport/Camden-Cycle-Counters-Phase-2/it3h-aqrf/about_data) | CSV         | [Open Government Licence v3.0](https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/)                 |

## How to run

This app will download bicycle traffic data in CSV format for all supported cities and ingest the data in an SQLite
database. Patience is a bliss, the whole process can take some time!

Once you have created the database, you can have a look at some query
examples [here](./examples-queries/ExampleQueries.md).

### Via Docker (Batteries included)

```bash
# build the docker image
make docker-build

# run it
make docker-run
```

The database should be created in a new `data` directory under the project root.

### On a local machine (Bring your own batteries)

You will need Java 11 or above.

VéloCityDB is released as a fat JAR that works on Linux, macOS and Windows.

Clone this repository and run the following:

```bash
# Build the fat JAR
make build-fatjar

# Run VéloCityDB
make run
```

If you need to run the app with specific flags, this is not currently supported via the Makefile. Download the latest
JAR from the releases
page and run the following:

```bash
mkdir data

# Passing specific flags
# See all available flags
java -jar velo-city-db-0.1.0-standalone.jar --help
# Ingest into a new database that will be created under /database/target/directory
java -jar velo-city-db-0.1.0-standalone.jar --data-directory-path $(pwd)/data
# Force re-download existing CSV files 
java -jar velo-city-db-0.1.0-standalone.jar --override-csv-files true --data-directory-path $(pwd)/data
```

Once the process has completed, you should have a new SQLite database located in the directory named `data`.

## How to run tests

```bash
# all tests
make test

# unit tests only
make test-unit

# integration tests only
make test-integration
```

## How to build a fat JAR

```bash
make build-fatjar
```

## How to add a new city

First of all, thank you for your interest in this project and in adding a new city to the database!

The codebase is in Kotlin. Contributions of all technical levels are welcome.

The best way to contribute is by raising a PR that includes both business logic and tests!

### The easier path ("Easy peasy lemon squeezy")

If a CSV version of the bicycle traffic data is available in a single file, you can have a look at how currently
supported cities are ingested in VéloCityDB. This consists in:

- Adding the relevant city name and endpoint to `src/main/resources/data-sources.yml`
    - The city name must be pascal cased, with alphanumeric characters only, i.e. `MyNewCity`.
- Writing a CSV parser for that city in the package located at `src/main/kotlin/parse`.
    - The CSV parser class must have a name of the pattern `MyNewCityCsvParser` where `MyNewCity` is the exact same name
      as the one used in `data-sources.yml`

### The more involved path ("I shall design a turbocharger all by myself")

If you need to introduce new logic, you can either convert the data into CSV file so that you can piggyback on the CSV
parsing code described above, or create completely new logic. :)

## What's in the name?

VéloCity is a play on words involving:

- On one hand, _Vélo_ - French for bicycle - and _City_
- On the other hand the word _velocity_ which can be roughly defined as "the speed and direction of motion of an
  object" (thank you, [Wikipedia](https://en.wikipedia.org/wiki/Velocity)).

## Wishlist

- Add more cities
- Ingest geolocation data where available
- For convenience, create a Docker container that packs a pre-configured analytics tool
    - e.g. a tool such as Metabase with pre-configured queries and dashboards to have a quick overview of VéloCityDB

## License

3-Clause BSD License.

See file named LICENSE at the root of the project.
