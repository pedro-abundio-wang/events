FROM postgres:14

# Install the packages which will be required to get everything to compile
RUN apt-get update \
    && apt-get -y install build-essential postgresql-server-dev-14 postgresql-14-wal2json

RUN apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy the configuration which will be passed down to the server
# (using a .sample file is the preferred way of doing it by the base Docker image)
COPY postgresql.conf.sample /usr/share/postgresql/postgresql.conf.sample

# Copy the script which will initialize the replication permissions
COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

# Initialize schema
COPY 0.activate-additional-scripts.sh /docker-entrypoint-initdb.d

COPY 1.initialize-database.sql /docker-entrypoint-initdb.d
COPY 2.initialize-database.sql /docker-entrypoint-initdb.d
COPY 3.initialize-database.sql /docker-entrypoint-initdb.d

RUN mkdir additional-scripts
RUN chown postgres -R /additional-scripts /docker-entrypoint-initdb.d

COPY 4.initialize-database-json.sql /additional-scripts
COPY 5.initialize-database-db-id.sql /additional-scripts
COPY 6.initialize-database-business.sql /additional-scripts

RUN touch /docker-entrypoint-initdb.d/4.initialize-database-json.sql
RUN touch /docker-entrypoint-initdb.d/5.initialize-database-db-id.sql
RUN touch /docker-entrypoint-initdb.d/6.initialize-database-business.sql
