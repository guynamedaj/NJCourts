import { S3Client, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3'
import { getSignedUrl } from '@aws-sdk/s3-request-presigner'

const s3Bucket = process.env.S3_BUCKET
const s3Region = process.env.AWS_REGION

if (!s3Bucket || !s3Region) {
  throw new Error('S3_BUCKET and AWS_REGION are required — configure AWS S3 in api/.env')
}

const s3Client = new S3Client({ region: s3Region })

const PRESIGNED_URL_TTL_SECONDS = 3600

export interface StoredPhoto {
  s3Key: string
  s3Url: string
}

export async function storePhoto(
  ticketId: string,
  fileName: string,
  buffer: Buffer,
  contentType: string,
): Promise<StoredPhoto> {
  const key = `tickets/${ticketId}/evidence/${fileName}`
  await s3Client.send(
    new PutObjectCommand({
      Bucket: s3Bucket,
      Key: key,
      Body: buffer,
      ContentType: contentType,
    }),
  )
  const s3Url = await presignKey(key)
  return { s3Key: key, s3Url }
}

export async function presignKey(key: string): Promise<string> {
  return getSignedUrl(
    s3Client,
    new GetObjectCommand({ Bucket: s3Bucket, Key: key }),
    { expiresIn: PRESIGNED_URL_TTL_SECONDS },
  )
}
