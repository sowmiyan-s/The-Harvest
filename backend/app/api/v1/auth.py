"""
Authentication endpoints: Registration, Login (JWT), and Profile inspection.
"""

from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel, EmailStr
from typing import Optional
from ...core.security import hash_password, verify_password, create_access_token, decode_access_token

router = APIRouter(prefix="/auth", tags=["Authentication"])

# In-memory user store for zero-friction demonstration & testing
USERS_DB: dict[str, dict] = {}


class RegisterRequest(BaseModel):
    email: EmailStr
    password: str
    full_name: str
    default_language: str = "en"


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    email: str


class UserProfileResponse(BaseModel):
    user_id: str
    email: str
    full_name: str
    default_language: str


@router.post("/register", response_model=TokenResponse)
async def register(req: RegisterRequest):
    if req.email in USERS_DB:
        raise HTTPException(status_code=400, detail="User with this email already exists")

    import uuid
    user_id = str(uuid.uuid4())
    USERS_DB[req.email] = {
        "id": user_id,
        "email": req.email,
        "hashed_password": hash_password(req.password),
        "full_name": req.full_name,
        "default_language": req.default_language,
    }

    token = create_access_token(subject=user_id)
    return TokenResponse(access_token=token, user_id=user_id, email=req.email)


@router.post("/login", response_model=TokenResponse)
async def login(req: LoginRequest):
    user = USERS_DB.get(req.email)
    if not user or not verify_password(req.password, user["hashed_password"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    token = create_access_token(subject=user["id"])
    return TokenResponse(access_token=token, user_id=user["id"], email=req.email)


@router.get("/me", response_model=UserProfileResponse)
async def get_me(token: str):
    payload = decode_access_token(token)
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid or expired token")

    user_id = payload.get("sub")
    for user in USERS_DB.values():
        if user["id"] == user_id:
            return UserProfileResponse(
                user_id=user["id"],
                email=user["email"],
                full_name=user["full_name"],
                default_language=user["default_language"],
            )

    raise HTTPException(status_code=404, detail="User not found")
