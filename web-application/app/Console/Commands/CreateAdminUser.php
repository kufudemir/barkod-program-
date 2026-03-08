<?php

namespace App\Console\Commands;

use App\Models\User;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\Hash;

class CreateAdminUser extends Command
{
    protected $signature = 'marketpos:create-admin
                            {email : Admin kullanıcısının e-posta adresi}
                            {password : Admin şifresi}
                            {--name=Admin : Gösterilecek kullanıcı adı}';

    protected $description = 'Filament admin kullanıcısı oluşturur veya günceller';

    public function handle(): int
    {
        $email = (string) $this->argument('email');
        $password = (string) $this->argument('password');
        $name = (string) $this->option('name');

        User::query()->updateOrCreate(
            ['email' => $email],
            [
                'name' => $name,
                'password' => Hash::make($password),
            ]
        );

        $this->info("Admin kullanıcısı hazır: {$email}");

        return self::SUCCESS;
    }
}

