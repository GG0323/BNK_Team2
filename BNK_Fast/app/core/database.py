import oracledb as db
from app.core.config import (
    DB_USER,
    DB_PW,
    DB_HOST,
    DB_PORT,
    DB_SERVICE_NAME
)

# DB connection
def get_connection():
    # dsn = Data Source Name
    dsn = db.makedsn(
        DB_HOST,
        DB_PORT,
        service_name=DB_SERVICE_NAME
    )

    conn = db.connect(
        user=DB_USER,
        password=DB_PW,
        dsn=dsn
    )

    return conn