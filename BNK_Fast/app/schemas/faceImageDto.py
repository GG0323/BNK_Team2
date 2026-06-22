from pydantic import BaseModel

class IdFaceImage(BaseModel):
    member_no:int
    idface_enc:str
    idface_nonce:str
    idface_tag:str