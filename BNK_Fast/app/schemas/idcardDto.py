from pydantic import BaseModel

class IdCard(BaseModel):
    member_no:int
    idcard_enc:str
    idcard_nonce:str
    idcard_tag:str